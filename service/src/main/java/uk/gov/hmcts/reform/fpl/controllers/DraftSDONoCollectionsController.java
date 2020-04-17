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
@RequestMapping("/callback/draft-standard-directions-no-collections")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DraftSDONoCollectionsController {
    private static final String JUDGE_AND_LEGAL_ADVISOR_KEY = "judgeAndLegalAdvisor";

    private final DocmosisDocumentGeneratorService docmosisService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CommonDirectionService commonDirectionService;
    private final Time time;
    private final UploadDocumentService uploadDocumentService;
    private final RequestData requestData;
    private final PrepareDirectionsForDataStoreService prepareDirectionsForDataStoreService;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDataExtractionService caseDataExtractionService;
    private final HearingBookingService hearingBookingService;
    private final OrdersLookupService ordersLookupService;
    private final ObjectMapper mapper;
    private final OrderValidationService orderValidationService;


    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackrequest) {
        CaseDetails caseDetailsss = callbackrequest.getCaseDetails();
        CaseData caseDataaa = mapper.convertValue(caseDetailsss.getData(), CaseData.class);

        String hearingDate = getFirstHearingStartDate(caseDataaa.getHearingDetails());

        caseDetailsss.getData().put("dateOfIssue", time.now().toLocalDate());

        Stream.of(DirectionAssignee.values()).forEach(assignee ->
            caseDetailsss.getData().put(assignee.toHearingDateField(), hearingDate));

        if (!isNull(caseDataaa.getStandardDirectionOrderNoCollections())) {
            Map<DirectionAssignee, List<Element<Direction>>> directions = sortDirectionsByAssignee(caseDataaa);

            directions.forEach((key, value) -> caseDetailsss.getData().put(key.getValue(), value));

            caseDetailsss.getData().put(JUDGE_AND_LEGAL_ADVISOR_KEY,
                caseDataaa.getStandardDirectionOrderNoCollections().getJudgeAndLegalAdvisor());
        }

        if (isNotEmpty(caseDataaa.getAllocatedJudge())) {
            caseDetailsss.getData().put(JUDGE_AND_LEGAL_ADVISOR_KEY, setAllocatedJudgeLabel(caseDataaa));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsss.getData())
            .build();
    }

    private JudgeAndLegalAdvisor setAllocatedJudgeLabel(CaseData caseData) {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder().build();

        if (isNotEmpty(caseData.getStandardDirectionOrderNoCollections())
            && isNotEmpty(caseData.getStandardDirectionOrderNoCollections().getJudgeAndLegalAdvisor())) {
            judgeAndLegalAdvisor = caseData.getStandardDirectionOrderNoCollections().getJudgeAndLegalAdvisor();
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
            .removeCustomDirections(caseData.getStandardDirectionOrderNoCollections().getDirections());

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
            .standardDirectionOrderNoCollections(Order.builder()
                .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
                .build())
            .build();

        //TODO: needs to be updated to display hidden values on separate lists
        //      prepareDirectionsForDataStoreService.persistHiddenDirectionValues(
        //      getConfigDirectionsWithHiddenValues(), updated.getStandardDirectionOrderNoSingleList().getDirections());

        Document document = getDocument(
            caseDataExtractionService.getStandardOrderDirectionData(updated,
                updated.getStandardDirectionOrderNoCollections()).toMap(mapper)
        );

        Order order = updated.getStandardDirectionOrderNoCollections().toBuilder()
            .orderDoc(DocumentReference.builder()
                .url(document.links.self.href)
                .binaryUrl(document.links.binary.href)
                .filename(document.originalDocumentName)
                .build())
            .build();

        caseDetails.getData().put("standardDirectionOrderNoCollections", order);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(
        @RequestBody CallbackRequest callbackRequest) throws IOException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> validationErrors = orderValidationService.validate(caseData,
            caseData.getStandardDirectionOrderNoCollections());
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
            .standardDirectionOrderNoCollections(Order.builder()
                .orderStatus(caseData.getStandardDirectionOrderNoCollections().getOrderStatus())
                .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
                .build())
            .build();

        //TODO: needs to be updated to display hidden values on separate lists
        //    prepareDirectionsForDataStoreService.persistHiddenDirectionValues(
        //    getConfigDirectionsWithHiddenValues(), updated.getStandardDirectionOrderNoCollections().getDirections());

        Document document = getDocument(
            caseDataExtractionService.getStandardOrderDirectionData(updated,
                updated.getStandardDirectionOrderNoCollections()).toMap(mapper)
        );

        Order order = updated.getStandardDirectionOrderNoCollections().toBuilder()
            .orderDoc(DocumentReference.builder()
                .url(document.links.self.href)
                .binaryUrl(document.links.binary.href)
                .filename(updated.getStandardDirectionOrderNoCollections().getOrderStatus().getDocumentTitle())
                .build())
            .build();

        caseDetails.getData().put("standardDirectionOrderNoCollections", order);
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

        Order standardDirectionOrderNoCollections = caseData.getStandardDirectionOrderNoCollections();
        if (standardDirectionOrderNoCollections.getOrderStatus() != OrderStatus.SEALED) {
            return;
        }

        coreCaseDataService.triggerEvent(
            callbackRequest.getCaseDetails().getJurisdiction(),
            callbackRequest.getCaseDetails().getCaseTypeId(),
            callbackRequest.getCaseDetails().getId(),
            "internal-changeState:Gatekeeping->PREPARE_FOR_HEARING"
        );

        if (standardDirectionOrderNoCollections.getOrderStatus() == SEALED) {
            coreCaseDataService.triggerEvent(
                callbackRequest.getCaseDetails().getJurisdiction(),
                callbackRequest.getCaseDetails().getCaseTypeId(),
                callbackRequest.getCaseDetails().getId(),
                "internal-change:SEND_DOCUMENT",
                Map.of("documentToBeSent", standardDirectionOrderNoCollections.getOrderDoc())
            );
            applicationEventPublisher.publishEvent(new StandardDirectionsOrderIssuedEvent(callbackRequest,
                requestData));
        }
    }

    private List<Element<Direction>> getConfigDirectionsWithHiddenValues() throws IOException {
        // constructDirectionForCCD requires LocalDateTime, but this value is not used in what is returned
        return ordersLookupService.getStandardDirectionOrderNoCollections().getDirections()
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
