package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisStandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.service.CommonDirectionService;
import uk.gov.hmcts.reform.fpl.service.DocumentService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.OrderValidationService;
import uk.gov.hmcts.reform.fpl.service.PrepareDirectionsForDataStoreService;
import uk.gov.hmcts.reform.fpl.service.StandardDirectionsService;
import uk.gov.hmcts.reform.fpl.service.ValidateGroupService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.docmosis.StandardDirectionOrderGenerationService;
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderRouteValidator;
import uk.gov.hmcts.reform.fpl.service.sdo.StandardDirectionsOrderService;
import uk.gov.hmcts.reform.fpl.service.sdo.UrgentGatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.validation.groups.DateOfIssueGroup;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.SDO;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.UDO;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.model.Directions.getAssigneeToDirectionMapping;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.removeAllocatedJudgeProperties;

// TODO: 03/09/2020 refactor logic into sdo service
@RestController
@RequestMapping("/callback/draft-standard-directions")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StandardDirectionsOrderController extends CallbackController {
    private static final String JUDGE_AND_LEGAL_ADVISOR_KEY = "judgeAndLegalAdvisor";
    private static final String STANDARD_DIRECTION_ORDER_KEY = "standardDirectionOrder";
    private static final String DATE_OF_ISSUE_KEY = "dateOfIssue";

    private final DocumentService documentService;
    private final StandardDirectionOrderGenerationService sdoGenerationService;
    private final CommonDirectionService commonDirectionService;
    private final CoreCaseDataService coreCaseDataService;
    private final PrepareDirectionsForDataStoreService prepareDirectionsForDataStoreService;
    private final OrderValidationService orderValidationService;
    private final ValidateGroupService validateGroupService;
    private final StandardDirectionsService standardDirectionsService;
    private final StandardDirectionsOrderService sdoService;
    private final NoticeOfProceedingsService nopService;
    private final UrgentGatekeepingOrderService urgentOrderService;
    private final GatekeepingOrderRouteValidator routeValidator;
    private final ObjectMapper mapper;

    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        StandardDirectionOrder standardDirectionOrder = caseData.getStandardDirectionOrder();
        GatekeepingOrderRoute sdoRouter = caseData.getSdoRouter();

        List<String> errors = routeValidator.allowAccessToEvent(caseData);
        if (!errors.isEmpty()) {
            return respond(caseDetails, errors);
        }

        if (standardDirectionsService.hasEmptyDirections(caseData)) {
            caseDetails.getData().putAll(standardDirectionsService.populateStandardDirections(caseData));
        }

        if (null != sdoRouter && null != standardDirectionOrder) {
            switch (sdoRouter) {
                case UPLOAD:
                    caseDetails.getData().put("currentSDO", standardDirectionOrder.getOrderDoc());
                    caseDetails.getData().put("useUploadRoute", YES);
                    caseDetails.getData().put(
                        JUDGE_AND_LEGAL_ADVISOR_KEY, sdoService.getJudgeAndLegalAdvisorFromSDO(caseData)
                    );
                    break;
                case SERVICE:
                    caseDetails.getData().put(
                        DATE_OF_ISSUE_KEY, sdoService.generateDateOfIssue(standardDirectionOrder)
                    );
                    caseDetails.getData().put("useServiceRoute", YES);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + sdoRouter);
            }
        }

        return respond(caseDetails);
    }

    // keeping the populate-date-of-issue mapping in case of any lag between merge and upload of ccd defs
    @PostMapping({"populate-date-of-issue", "/pre-populate/mid-event"})
    public CallbackResponse handleMidEventPrePopulation(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        GatekeepingOrderRoute route = caseData.getSdoRouter();
        if (SERVICE == route) {
            caseDetails.getData().put(
                DATE_OF_ISSUE_KEY, sdoService.generateDateOfIssue(caseData.getStandardDirectionOrder())
            );
        }

        // see RDM-9147
        DocumentReference preparedSDO = caseData.getPreparedSDO();
        if (null != preparedSDO && preparedSDO.isEmpty()) {
            caseDetails.getData().remove("preparedSDO");
        }

        caseDetails.getData().put(JUDGE_AND_LEGAL_ADVISOR_KEY, sdoService.getJudgeAndLegalAdvisorFromSDO(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/date-of-issue/mid-event")
    public CallbackResponse handleMidEventIssueDate(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<String> errors = validateGroupService.validateGroup(caseData, DateOfIssueGroup.class);

        if (!errors.isEmpty()) {
            return respond(caseDetails, errors);
        }

        String hearingDate = getFirstHearingStartDate(caseData);

        Stream.of(DirectionAssignee.values()).forEach(
            assignee -> caseDetails.getData().put(assignee.toHearingDateField(), hearingDate)
        );

        caseDetails.getData().put(JUDGE_AND_LEGAL_ADVISOR_KEY, sdoService.getJudgeAndLegalAdvisorFromSDO(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/service-route/mid-event")
    public CallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(
            caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()
        );

        StandardDirectionOrder order = StandardDirectionOrder.builder()
            .directions(commonDirectionService.combineAllDirections(caseData))
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
            .build();

        persistHiddenValues(caseData.getFirstHearing().orElse(null), order.getDirections());

        CaseData updated = caseData.toBuilder().standardDirectionOrder(order).build();

        DocmosisStandardDirectionOrder templateData = sdoGenerationService.getTemplateData(updated);
        var docmosisTemplate = Objects.nonNull(caseData.getGatekeepingOrderRouter()) ? SDO : UDO;
        Document document = documentService.getDocumentFromDocmosisOrderTemplate(templateData, docmosisTemplate);

        order.setDirectionsToEmptyList();
        order.setOrderDocReferenceFromDocument(document);

        caseDetails.getData().put(STANDARD_DIRECTION_ORDER_KEY, order);
        // work around as I could not get the page hiding when using the [STATE] metadata field
        caseDetails.getData().put(
            "showNoticeOfProceedings", YesNo.from(GATEKEEPING == caseData.getState())
        );

        return respond(caseDetails);
    }

    @PostMapping("/upload-route/mid-event")
    public CallbackResponse handleUploadMidEvent(@RequestBody CallbackRequest request) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        CaseData caseDataBefore = getCaseDataBefore(request);

        StandardDirectionOrder sdo = sdoService.buildTemporarySDO(caseData, caseDataBefore.getStandardDirectionOrder());

        caseDetails.getData().put(STANDARD_DIRECTION_ORDER_KEY, sdo);
        // work around as I could not get the page hiding when using the [STATE] metadata field
        caseDetails.getData().put(
            "showNoticeOfProceedings", YesNo.from(GATEKEEPING == caseData.getState())
        );

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public CallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        GatekeepingOrderRoute sdoRouter = caseData.getSdoRouter();

        List<String> errors = orderValidationService.validate(caseData);
        if (!errors.isEmpty()) {
            return respond(caseDetails, errors);
        }

        // assigning default value to make sonar happy
        // its code analysis can't seem to pick up on the fact that the npe can't occur in the upcoming if
        StandardDirectionOrder order = StandardDirectionOrder.builder().build();
        switch (sdoRouter) {
            case SERVICE:
                JudgeAndLegalAdvisor judgeAndLegalAdvisor = getSelectedJudge(
                    caseData.getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge()
                );

                removeAllocatedJudgeProperties(judgeAndLegalAdvisor);

                //combine all directions from collections
                List<Element<Direction>> combinedDirections = commonDirectionService.combineAllDirections(caseData);

                persistHiddenValues(caseData.getFirstHearing().orElse(null), combinedDirections);

                //place directions with hidden values back into case details
                Map<DirectionAssignee, List<Element<Direction>>> directions = sortDirectionsByAssignee(
                    combinedDirections
                );
                directions.forEach((key, value) -> caseDetails.getData().put(key.getValue(), value));

                order = StandardDirectionOrder.builder()
                    .directions(commonDirectionService.removeUnnecessaryDirections(combinedDirections))
                    .orderStatus(caseData.getStandardDirectionOrder().getOrderStatus())
                    .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                    .dateOfIssue(formatLocalDateToString(caseData.getDateOfIssue(), DATE))
                    .build();

                //add sdo to case data for document generation
                CaseData updated = caseData.toBuilder().standardDirectionOrder(order).build();

                //generate sdo document
                DocmosisStandardDirectionOrder templateData = sdoGenerationService.getTemplateData(updated);
                var docmosisTemplate =
                    Objects.nonNull(caseData.getGatekeepingOrderRouter()) ? SDO : UDO;
                Document document =
                    documentService.getDocumentFromDocmosisOrderTemplate(templateData, docmosisTemplate);

                //add document to order
                order.setOrderDocReferenceFromDocument(document);
                caseDetails.getData().put(STANDARD_DIRECTION_ORDER_KEY, order);
                break;
            case UPLOAD:
                order = sdoService.buildOrderFromUpload(caseData.getStandardDirectionOrder(),
                    caseData.getCourt(),
                    caseData.getSealType());
                caseDetails.getData().put(STANDARD_DIRECTION_ORDER_KEY, order);
                break;
        }

        List<String> tempFields = GatekeepingOrderEventData.temporaryFields();
        tempFields.addAll(List.of(JUDGE_AND_LEGAL_ADVISOR_KEY, DATE_OF_ISSUE_KEY, "preparedSDO", "currentSDO",
            "replacementSDO", "useServiceRoute", "useUploadRoute", "noticeOfProceedings", "showNoticeOfProceedings"
        ));

        if (order.isSealed()) {
            caseDetails.getData().put("state", CASE_MANAGEMENT);
            tempFields.add("sdoRouter");

            if (GATEKEEPING == caseData.getState()) {
                List<DocmosisTemplates> docmosisTemplateTypes =
                    caseData.getNoticeOfProceedings().mapProceedingTypesToDocmosisTemplate();

                List<Element<DocumentBundle>> newNoP = nopService.uploadNoticesOfProceedings(
                    caseData, docmosisTemplateTypes
                );

                caseDetails.getData().put("noticeOfProceedingsBundle", newNoP);
            }
        }

        removeTemporaryFields(caseDetails, tempFields.toArray(String[]::new));

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest request) {
        //Do nothing event deprecated
    }

    private String getFirstHearingStartDate(CaseData caseData) {
        return caseData.getFirstHearingOfType(HearingType.CASE_MANAGEMENT)
            .map(hearing -> formatLocalDateTimeBaseUsingFormat(hearing.getStartDate(), DATE_TIME))
            .orElse("Please enter a hearing date");
    }

    private Map<DirectionAssignee, List<Element<Direction>>> sortDirectionsByAssignee(List<Element<Direction>> list) {
        List<Element<Direction>> nonCustomDirections = commonDirectionService.removeCustomDirections(list);

        return getAssigneeToDirectionMapping(nonCustomDirections);
    }

    private void persistHiddenValues(HearingBooking firstHearing, List<Element<Direction>> directions) {
        List<Element<Direction>> standardDirections = standardDirectionsService.getDirections(firstHearing);

        prepareDirectionsForDataStoreService.persistHiddenDirectionValues(standardDirections, directions);
    }
}
