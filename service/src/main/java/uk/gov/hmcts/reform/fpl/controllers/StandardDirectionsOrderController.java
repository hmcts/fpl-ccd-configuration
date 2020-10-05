package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute;
import uk.gov.hmcts.reform.fpl.events.StandardDirectionsOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.CommonDirectionService;
import uk.gov.hmcts.reform.fpl.service.DocumentService;
import uk.gov.hmcts.reform.fpl.service.OrderValidationService;
import uk.gov.hmcts.reform.fpl.service.PrepareDirectionsForDataStoreService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.docmosis.StandardDirectionOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.sdo.StandardDirectionsOrderService;
import uk.gov.hmcts.reform.fpl.validation.groups.DateOfIssueGroup;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.SDO;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SDORoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.model.Directions.getAssigneeToDirectionMapping;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.buildAllocatedJudgeLabel;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.prepareJudgeFields;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.removeAllocatedJudgeProperties;

// TODO: 03/09/2020 refactor logic into sdo service
@Api
@RestController
@RequestMapping("/callback/draft-standard-directions")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionsOrderController extends CallbackController {
    private final DocumentService documentService;
    private final StandardDirectionOrderGenerationService standardDirectionOrderGenerationService;
    private final CommonDirectionService commonDirectionService;
    private final CoreCaseDataService coreCaseDataService;
    private final PrepareDirectionsForDataStoreService prepareDirectionsForDataStoreService;
    private final OrderValidationService orderValidationService;
    private final ValidateGroupService validateGroupService;
    private final StandardDirectionsService standardDirectionsService;
    private final StandardDirectionsOrderService sdoService;

    private static final String JUDGE_AND_LEGAL_ADVISOR_KEY = "judgeAndLegalAdvisor";

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);
        StandardDirectionOrder standardDirectionOrder = caseData.getStandardDirectionOrder();
        SDORoute sdoRouter = caseData.getSdoRouter();

        if (sdoRouter != null && standardDirectionOrder != null) {
            switch (sdoRouter) {
                case UPLOAD:
                    data.put("currentSDO", standardDirectionOrder.getOrderDoc());
                    data.put("useUploadRoute", YES);
                    break;
                case SERVICE:
                    data.put("dateOfIssue", sdoService.generateDateOfIssue(standardDirectionOrder));
                    data.put("useServiceRoute", YES);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + sdoRouter);
            }
        }

        return respond(caseDetails);
    }

    @PostMapping("/populate-date-of-issue/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventPopulateIssueDate(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        if (caseData.getSdoRouter() == SERVICE) {
            data.put("dateOfIssue", sdoService.generateDateOfIssue(caseData.getStandardDirectionOrder()));
        }

        // see RDM-9147
        DocumentReference preparedSDO = caseData.getPreparedSDO();
        if (preparedSDO != null && preparedSDO.isEmpty()) {
            data.remove("preparedSDO");
        }

        return respond(caseDetails);
    }

    @PostMapping("/date-of-issue/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEventIssueDate(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = validateGroupService.validateGroup(caseData, DateOfIssueGroup.class);

        if (!errors.isEmpty()) {
            return respond(caseDetails, errors);
        }

        String hearingDate = getFirstHearingStartDate(caseData);

        Stream.of(DirectionAssignee.values()).forEach(assignee ->
            caseDetails.getData().put(assignee.toHearingDateField(), hearingDate));

        StandardDirectionOrder standardDirectionOrder = caseData.getStandardDirectionOrder();

        if (standardDirectionOrder != null) {
            caseDetails.getData().put(JUDGE_AND_LEGAL_ADVISOR_KEY, standardDirectionOrder.getJudgeAndLegalAdvisor());
        }

        if (isNotEmpty(caseData.getAllocatedJudge())) {
            caseDetails.getData().put(JUDGE_AND_LEGAL_ADVISOR_KEY, prepareJudge(caseData));
        }

        return respond(caseDetails);
    }

    @PostMapping("/service-route/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(caseData.getJudgeAndLegalAdvisor(),
            caseData.getAllocatedJudge());

        StandardDirectionOrder order = StandardDirectionOrder.builder()
            .directions(commonDirectionService.combineAllDirections(caseData))
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
            .build();

        persistHiddenValues(caseData.getFirstHearing().orElse(null), order.getDirections());

        CaseData updated = caseData.toBuilder().standardDirectionOrder(order).build();

        DocmosisStandardDirectionOrder templateData = standardDirectionOrderGenerationService.getTemplateData(updated);
        Document document = documentService.getDocumentFromDocmosisOrderTemplate(templateData, SDO);

        order.setDirectionsToEmptyList();
        order.setOrderDocReferenceFromDocument(document);

        caseDetails.getData().put("standardDirectionOrder", order);

        return respond(caseDetails);
    }

    @PostMapping("/upload-route/mid-event")
    public CallbackResponse handleUploadMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(request);

        StandardDirectionOrder order = sdoService.buildTemporarySDO(
            caseData,
            caseDataBefore.getStandardDirectionOrder()
        );

        caseDetails.getData().put("standardDirectionOrder", order);

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public CallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) throws Exception {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = orderValidationService.validate(caseData);
        if (!errors.isEmpty()) {
            return respond(caseDetails, errors);
        }

        StandardDirectionOrder order;
        SDORoute sdoRouter = caseData.getSdoRouter();
        if (SERVICE == sdoRouter) {
            JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(
                caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()
            );

            removeAllocatedJudgeProperties(judgeAndLegalAdvisor);

            //combine all directions from collections
            List<Element<Direction>> combinedDirections = commonDirectionService.combineAllDirections(caseData);

            persistHiddenValues(caseData.getFirstHearing().orElse(null), combinedDirections);

            //place directions with hidden values back into case details
            Map<DirectionAssignee, List<Element<Direction>>> directions = sortDirectionsByAssignee(combinedDirections);
            directions.forEach((key, value) -> data.put(key.getValue(), value));

            order = StandardDirectionOrder.builder()
                .directions(commonDirectionService.removeUnnecessaryDirections(combinedDirections))
                .orderStatus(caseData.getStandardDirectionOrder().getOrderStatus())
                .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
                .build();

            //add sdo to case data for document generation
            CaseData updated = caseData.toBuilder().standardDirectionOrder(order).build();

            //generate sdo document
            DocmosisStandardDirectionOrder templateData = standardDirectionOrderGenerationService.getTemplateData(
                updated);
            Document document = documentService.getDocumentFromDocmosisOrderTemplate(templateData, SDO);

            //add document to order
            order.setOrderDocReferenceFromDocument(document);
        } else {
            StandardDirectionOrder currentOrder = caseData.getStandardDirectionOrder();

            order = sdoService.buildOrderFromUpload(currentOrder);
        }

        data.put("standardDirectionOrder", order);
        removeTemporaryFields(caseDetails,
            JUDGE_AND_LEGAL_ADVISOR_KEY,
            "dateOfIssue",
            "preparedSDO",
            "currentSDO",
            "replacementSDO",
            "useServiceRoute",
            "useUploadRoute"
        );

        if (order.getOrderStatus() == SEALED) {
            data.put("state", State.CASE_MANAGEMENT);
            removeTemporaryFields(caseDetails, "sdoRouter");
        }

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        StandardDirectionOrder standardDirectionOrder = caseData.getStandardDirectionOrder();
        if (standardDirectionOrder.getOrderStatus() != SEALED) {
            return;
        }

        coreCaseDataService.triggerEvent(
            callbackRequest.getCaseDetails().getJurisdiction(),
            callbackRequest.getCaseDetails().getCaseTypeId(),
            callbackRequest.getCaseDetails().getId(),
            "internal-change-SEND_DOCUMENT",
            Map.of("documentToBeSent", standardDirectionOrder.getOrderDoc())
        );
        publishEvent(new StandardDirectionsOrderIssuedEvent(caseData));
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

    private String getFirstHearingStartDate(CaseData caseData) {
        return caseData.getFirstHearing()
            .map(hearing -> formatLocalDateTimeBaseUsingFormat(hearing.getStartDate(), DATE_TIME))
            .orElse("Please enter a hearing date");
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
