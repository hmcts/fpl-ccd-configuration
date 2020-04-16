package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.CommonDirectionService;
import uk.gov.hmcts.reform.fpl.service.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.OrderValidationService;
import uk.gov.hmcts.reform.fpl.service.OrdersLookupService;
import uk.gov.hmcts.reform.fpl.service.PrepareDirectionsForDataStoreService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.removeAllocatedJudgeProperties;

@Api
@RestController
@RequestMapping("/callback/draft-standard-directions-no-single-list")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DraftSDONoSingleListController {
    private final ObjectMapper mapper;
    private final DocmosisDocumentGeneratorService docmosisService;
    private final UploadDocumentService uploadDocumentService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final CommonDirectionService commonDirectionService;
    private final OrdersLookupService ordersLookupService;
    private final CoreCaseDataService coreCaseDataService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PrepareDirectionsForDataStoreService prepareDirectionsForDataStoreService;
    private final OrderValidationService orderValidationService;
    private final HearingBookingService hearingBookingService;
    private final Time time;
    private final RequestData requestData;

    private static final String JUDGE_AND_LEGAL_ADVISOR_KEY = "judgeAndLegalAdvisor";

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetails = callbackrequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        String hearingDate = getFirstHearingStartDate(caseData.getHearingDetails());

        caseDetails.getData().put("dateOfIssue", time.now().toLocalDate());

        Stream.of(DirectionAssignee.values()).forEach(assignee ->
            caseDetails.getData().put(assignee.toHearingDateField(), hearingDate));

        if (!isNull(caseData.getStandardDirectionOrderNoSingleList())) {
            Map<DirectionAssignee, List<Element<Direction>>> directions = sortDirectionsByAssignee(caseData);

            directions.forEach((key, value) -> caseDetails.getData().put(key.getValue(), value));

            caseDetails.getData().put(JUDGE_AND_LEGAL_ADVISOR_KEY,
                caseData.getStandardDirectionOrderNoSingleList().getJudgeAndLegalAdvisor());
        }

        if (isNotEmpty(caseData.getAllocatedJudge())) {
            caseDetails.getData().put(JUDGE_AND_LEGAL_ADVISOR_KEY, setAllocatedJudgeLabel(caseData));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    private JudgeAndLegalAdvisor setAllocatedJudgeLabel(CaseData caseData) {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder().build();

        if (isNotEmpty(caseData.getStandardDirectionOrderNoSingleList())
            && isNotEmpty(caseData.getStandardDirectionOrderNoSingleList().getJudgeAndLegalAdvisor())) {
            judgeAndLegalAdvisor = caseData.getStandardDirectionOrderNoSingleList().getJudgeAndLegalAdvisor();
        }

        judgeAndLegalAdvisor.setAllocatedJudgeLabel(buildAllocatedJudgeLabel(caseData.getAllocatedJudge()));

        return judgeAndLegalAdvisor;
    }

    private String getFirstHearingStartDate(List<Element<HearingBooking>> hearings) {
        return hearingBookingService.getFirstHearing(hearings)
            .map(hearing -> formatLocalDateTimeBaseUsingFormat(hearing.getStartDate(), DATE_TIME))
            .orElse("Please enter a hearing date");
    }

    private Map<DirectionAssignee, List<Element<Direction>>> sortDirectionsByAssignee(CaseData caseData) {
        List<Element<Direction>> nonCustomDirections = commonDirectionService
            .removeCustomDirections(caseData.getStandardDirectionOrderNoSingleList().getDirections());

        return commonDirectionService.sortDirectionsByAssignee(nonCustomDirections);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(
        @RequestBody CallbackRequest callbackRequest) throws IOException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(caseData.getJudgeAndLegalAdvisor(),
            caseData.getAllocatedJudge());

        CaseData updated = caseData.toBuilder()
            .standardDirectionOrderNoSingleList(Order.builder()
                .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
                .build())
            .build();

        //TODO: needs to be updated to display hidden values on separate lists
//        prepareDirectionsForDataStoreService.persistHiddenDirectionValues(
//            getConfigDirectionsWithHiddenValues(), updated.getStandardDirectionOrderNoSingleList().getDirections());

        Document document = getDocument(
            caseDataExtractionService.getStandardOrderDirectionData(updated, updated.getStandardDirectionOrderNoSingleList()).toMap(mapper)
        );

        Order order = updated.getStandardDirectionOrderNoSingleList().toBuilder()
            .orderDoc(DocumentReference.builder()
                .url(document.links.self.href)
                .binaryUrl(document.links.binary.href)
                .filename(document.originalDocumentName)
                .build())
            .build();

        caseDetails.getData().put("standardDirectionOrderNoSingleList", order);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) throws IOException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> validationErrors = orderValidationService.validate(caseData, caseData.getStandardDirectionOrderNoSingleList());
        if (!validationErrors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .errors(validationErrors)
                .build();
        }

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(
            caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge());

        removeAllocatedJudgeProperties(judgeAndLegalAdvisor);

        CaseData updated = caseData.toBuilder()
            .standardDirectionOrderNoSingleList(Order.builder()
                .orderStatus(caseData.getStandardDirectionOrderNoSingleList().getOrderStatus())
                .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
                .build())
            .build();

        prepareDirectionsForDataStoreService.persistHiddenDirectionValues(
            getConfigDirectionsWithHiddenValues(), updated.getStandardDirectionOrderNoSingleList().getDirections());

        Document document = getDocument(
            caseDataExtractionService.getStandardOrderDirectionData(updated, updated.getStandardDirectionOrderNoSingleList()).toMap(mapper)
        );

        Order order = updated.getStandardDirectionOrderNoSingleList().toBuilder()
            .orderDoc(DocumentReference.builder()
                .url(document.links.self.href)
                .binaryUrl(document.links.binary.href)
                .filename(updated.getStandardDirectionOrderNoSingleList().getOrderStatus().getDocumentTitle())
                .build())
            .build();

        caseDetails.getData().put("standardDirectionOrderNoSingleList", order);
        caseDetails.getData().remove(JUDGE_AND_LEGAL_ADVISOR_KEY);
        caseDetails.getData().remove("dateOfIssue");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);

        Order standardDirectionOrderNoSingleList = caseData.getStandardDirectionOrderNoSingleList();
        if (standardDirectionOrderNoSingleList.getOrderStatus() != OrderStatus.SEALED) {
            return;
        }

        coreCaseDataService.triggerEvent(
            callbackRequest.getCaseDetails().getJurisdiction(),
            callbackRequest.getCaseDetails().getCaseTypeId(),
            callbackRequest.getCaseDetails().getId(),
            "internal-changeState:Gatekeeping->PREPARE_FOR_HEARING"
        );

        if (standardDirectionOrderNoSingleList.getOrderStatus() == SEALED) {
            coreCaseDataService.triggerEvent(
                callbackRequest.getCaseDetails().getJurisdiction(),
                callbackRequest.getCaseDetails().getCaseTypeId(),
                callbackRequest.getCaseDetails().getId(),
                "internal-change:SEND_DOCUMENT",
                Map.of("documentToBeSent", standardDirectionOrderNoSingleList.getOrderDoc())
            );
            applicationEventPublisher.publishEvent(new StandardDirectionsOrderIssuedEvent(callbackRequest,
                requestData));
        }
    }

    private List<Element<Direction>> getConfigDirectionsWithHiddenValues() throws IOException {
        // constructDirectionForCCD requires LocalDateTime, but this value is not used in what is returned
        return ordersLookupService.getStandardDirectionOrderNoSingleList().getDirections()
            .stream()
            .map(direction -> commonDirectionService.constructDirectionForCCD(direction, LocalDateTime.now()))
            .collect(Collectors.toList());
    }

    private Document getDocument(Map<String, Object> templateData) {
        DocmosisDocument document = docmosisService.generateDocmosisDocument(templateData, DocmosisTemplates.SDO);

        String docTitle = document.getDocumentTitle();

        if (isNotEmpty(templateData.get("draftbackground"))) {
            docTitle = "draft-" + document.getDocumentTitle();
        }

        return uploadDocumentService.uploadPDF(document.getBytes(), docTitle);
    }
}
