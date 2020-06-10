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
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.CommonDirectionService;
import uk.gov.hmcts.reform.fpl.service.DocumentService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.OrderValidationService;
import uk.gov.hmcts.reform.fpl.service.PrepareDirectionsForDataStoreService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.docmosis.StandardDirectionOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.validation.groups.DateOfIssueGroup;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.SDO;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.model.Directions.getAssigneeToDirectionMapping;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.parseLocalDateFromStringUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.prepareJudgeFields;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.removeAllocatedJudgeProperties;

@Api
@RestController
@RequestMapping("/callback/draft-standard-directions")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DraftOrdersController {
    private final ObjectMapper mapper;
    private final DocumentService documentService;
    private final StandardDirectionOrderGenerationService standardDirectionOrderGenerationService;
    private final CommonDirectionService commonDirectionService;
    private final CoreCaseDataService coreCaseDataService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PrepareDirectionsForDataStoreService prepareDirectionsForDataStoreService;
    private final OrderValidationService orderValidationService;
    private final HearingBookingService hearingBookingService;
    private final Time time;
    private final ValidateGroupService validateGroupService;
    private final StandardDirectionsService standardDirectionsService;

    private static final String JUDGE_AND_LEGAL_ADVISOR_KEY = "judgeAndLegalAdvisor";

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
        LocalDate dateOfIssue = time.now().toLocalDate();
        Order standardDirectionOrder = caseData.getStandardDirectionOrder();

        if (standardDirectionOrder != null && standardDirectionOrder.getDateOfIssue() != null) {
            dateOfIssue = parseLocalDateFromStringUsingFormat(standardDirectionOrder.getDateOfIssue(), DATE);
        }

        caseDetails.getData().put("dateOfIssue", dateOfIssue);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/date-of-issue/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventIssueDate(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> errors = validateGroupService.validateGroup(caseData, DateOfIssueGroup.class);

        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .errors(errors)
                .build();
        }

        String hearingDate = getFirstHearingStartDate(caseData.getHearingDetails());

        Stream.of(DirectionAssignee.values()).forEach(assignee ->
            caseDetails.getData().put(assignee.toHearingDateField(), hearingDate));

        Order standardDirectionOrder = caseData.getStandardDirectionOrder();

        if (standardDirectionOrder != null) {
            caseDetails.getData().put(JUDGE_AND_LEGAL_ADVISOR_KEY, standardDirectionOrder.getJudgeAndLegalAdvisor());
        }

        if (isNotEmpty(caseData.getAllocatedJudge())) {
            caseDetails.getData().put(JUDGE_AND_LEGAL_ADVISOR_KEY, prepareJudge(caseData));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(caseData.getJudgeAndLegalAdvisor(),
            caseData.getAllocatedJudge());

        Order order = Order.builder()
            .directions(commonDirectionService.combineAllDirections(caseData))
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
            .build();

        persistHiddenValues(getFirstHearing(caseData.getHearingDetails()), order.getDirections());

        CaseData updated = caseData.toBuilder().standardDirectionOrder(order).build();

        DocmosisStandardDirectionOrder templateData = standardDirectionOrderGenerationService.getTemplateData(updated);
        Document document = documentService.getDocumentFromDocmosisOrderTemplate(templateData, SDO);

        order.setDirectionsToEmptyList();
        order.setOrderDocReferenceFromDocument(document);

        caseDetails.getData().put("standardDirectionOrder", order);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> validationErrors = orderValidationService.validate(caseData);
        if (!validationErrors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDetails.getData())
                .errors(orderValidationService.validate(caseData))
                .build();
        }

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(
            caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge());

        removeAllocatedJudgeProperties(judgeAndLegalAdvisor);

        //combine all directions from collections
        List<Element<Direction>> combinedDirections = commonDirectionService.combineAllDirections(caseData);

        persistHiddenValues(getFirstHearing(caseData.getHearingDetails()), combinedDirections);

        //place directions with hidden values back into case details
        Map<DirectionAssignee, List<Element<Direction>>> directions = sortDirectionsByAssignee(combinedDirections);
        directions.forEach((key, value) -> caseDetails.getData().put(key.getValue(), value));

        Order order = Order.builder()
            .directions(commonDirectionService.removeUnnecessaryDirections(combinedDirections))
            .orderStatus(caseData.getStandardDirectionOrder().getOrderStatus())
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
            .build();

        //add sdo to case data for document generation
        CaseData updated = caseData.toBuilder().standardDirectionOrder(order).build();

        //generate sdo document
        DocmosisStandardDirectionOrder templateData = standardDirectionOrderGenerationService.getTemplateData(updated);
        Document document = documentService.getDocumentFromDocmosisOrderTemplate(templateData, SDO);

        //add document to order
        order.setOrderDocReferenceFromDocument(document);

        caseDetails.getData().put("standardDirectionOrder", order);
        caseDetails.getData().remove(JUDGE_AND_LEGAL_ADVISOR_KEY);
        caseDetails.getData().remove("dateOfIssue");

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetails.getData())
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);

        Order standardDirectionOrder = caseData.getStandardDirectionOrder();
        if (standardDirectionOrder.getOrderStatus() != OrderStatus.SEALED) {
            return;
        }

        coreCaseDataService.triggerEvent(
            callbackRequest.getCaseDetails().getJurisdiction(),
            callbackRequest.getCaseDetails().getCaseTypeId(),
            callbackRequest.getCaseDetails().getId(),
            "internal-changeState-Gatekeeping->PREPARE_FOR_HEARING"
        );

        if (standardDirectionOrder.getOrderStatus() == SEALED) {
            coreCaseDataService.triggerEvent(
                callbackRequest.getCaseDetails().getJurisdiction(),
                callbackRequest.getCaseDetails().getCaseTypeId(),
                callbackRequest.getCaseDetails().getId(),
                "internal-change-SEND_DOCUMENT",
                Map.of("documentToBeSent", standardDirectionOrder.getOrderDoc())
            );
            applicationEventPublisher.publishEvent(new StandardDirectionsOrderIssuedEvent(callbackRequest));
        }
    }

    private JudgeAndLegalAdvisor prepareJudge(CaseData caseData) {
        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder().build();

        if (isNotEmpty(caseData.getStandardDirectionOrder())
            && isNotEmpty(caseData.getStandardDirectionOrder().getJudgeAndLegalAdvisor())) {
            judgeAndLegalAdvisor = prepareJudgeFields(caseData.getStandardDirectionOrder().getJudgeAndLegalAdvisor(),
                caseData.getAllocatedJudge());
        }

        judgeAndLegalAdvisor.setAllocatedJudgeLabel(buildAllocatedJudgeLabel(caseData.getAllocatedJudge()));

        return judgeAndLegalAdvisor;
    }

    private String getFirstHearingStartDate(List<Element<HearingBooking>> hearings) {
        return ofNullable(getFirstHearing(hearings))
            .map(hearing -> formatLocalDateTimeBaseUsingFormat(hearing.getStartDate(), DATE_TIME))
            .orElse("Please enter a hearing date");
    }

    private HearingBooking getFirstHearing(List<Element<HearingBooking>> hearingBookings) {
        return hearingBookingService.getFirstHearing(hearingBookings).orElse(null);
    }

    private Map<DirectionAssignee, List<Element<Direction>>> sortDirectionsByAssignee(List<Element<Direction>> list) {
        List<Element<Direction>> nonCustomDirections = commonDirectionService.removeCustomDirections(list);

        return getAssigneeToDirectionMapping(nonCustomDirections);
    }

    private void persistHiddenValues(HearingBooking firstHearing,
                                     List<Element<Direction>> directions) {
        List<Element<Direction>> standardDirections = standardDirectionsService.getDirections(firstHearing);

        prepareDirectionsForDataStoreService.persistHiddenDirectionValues(standardDirections, directions);
    }
}
