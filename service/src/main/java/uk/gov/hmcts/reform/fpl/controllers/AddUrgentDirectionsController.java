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
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.UrgentDirectionsRoute;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.CourtLevelAllocationService;
import uk.gov.hmcts.reform.fpl.service.GatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderDataFixer;
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderEventNotificationDecider;
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderRouteValidator;
import uk.gov.hmcts.reform.fpl.service.sdo.UrgentGatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.allNotNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Api
@RestController
@RequestMapping("/callback/add-urgent-directions")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddUrgentDirectionsController extends CallbackController {

    private final GatekeepingOrderService orderService;

    private final CourtLevelAllocationService allocationService;
    private final GatekeepingOrderRouteValidator routeValidator;
    private final GatekeepingOrderDataFixer dataFixer;
    private final CaseConverter caseConverter;
    private final NoticeOfProceedingsService nopService;
    private final CoreCaseDataService coreCaseDataService;
    private final UrgentGatekeepingOrderService urgentOrderService;
    private final GatekeepingOrderEventNotificationDecider notificationDecider;


    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        final CaseDetailsMap data = dataFixer.addLanguageRequirement(caseDetailsMap(callbackRequest.getCaseDetails()));

        //TODO: check if this is the order we need to be looking at
        final StandardDirectionOrder draftOrder = caseData.getStandardDirectionOrder();

        final UrgentDirectionsRoute draftOrderRoute = caseData.getUrgentDirectionsRouter();

        //TODO: check what order we need to validate against here
        final List<String> errors = routeValidator.allowAccessToEvent(caseData);

        if (isNotEmpty(errors)) {
            return respond(data, errors);
        }

        boolean hasDraft = allNotNull(draftOrderRoute, draftOrder);

        if (hasDraft) {
            switch (draftOrderRoute) {
                case UPLOAD:
                    data.put("currentSDO", draftOrder.getOrderDoc());
                    data.put("useUploadRoute", YES);
                    break;
                case SERVICE:
                    data.put("useServiceRoute", YES);
                    break;
            }
        }

        if (nonNull(caseData.getAllocatedJudge())) {
            //TODO: do we need to create an urgentDirectionsOrderEventData
            data.put("gatekeepingOrderIssuingJudge", orderService.setAllocatedJudgeLabel(caseData.getAllocatedJudge(),
                caseData.getGatekeepingOrderEventData().getGatekeepingOrderIssuingJudge()));
        }

        final Allocation allocationDecision = allocationService.createDecision(caseData);
        data.put("allocationDecision", allocationDecision);

        return respond(data);
    }

    @PostMapping("/pre-populate/mid-event")
    public CallbackResponse handleMidEventPrePopulation(@RequestBody CallbackRequest request) {
        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        if (Objects.nonNull(caseData.getUrgentDirectionsRouter())) {
            final List<String> errors = routeValidator.allowAccessToUrgentHearingRoute(caseData);
            if (!errors.isEmpty()) {
                return respond(caseDetails, errors);
            }
            caseDetails.getData().putAll(caseConverter.toMap(urgentOrderService.prePopulate(caseData)));
        }

        return respond(caseDetails);
    }

    @PostMapping("/direction-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse populateSelectedDirections(@RequestBody CallbackRequest request) {
        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        if (caseData.getGatekeepingOrderRouter() != SERVICE) {
            throw new UnsupportedOperationException(format(
                "The direction-selection callback does not support %s route ", caseData.getGatekeepingOrderRouter()));
        }

        orderService.populateStandardDirections(caseDetails);

        orderService.getHearing(caseData)
            .map(HearingBooking::getStartDate)
            .map(hearingDate -> formatLocalDateTimeBaseUsingFormat(hearingDate, DATE_TIME))
            .ifPresent(hearingDate -> {
                caseDetails.getData().put("gatekeepingOrderHasHearing1", YES);
                caseDetails.getData().put("gatekeepingOrderHasHearing2", YES);
                caseDetails.getData().put("gatekeepingOrderHearingDate1", hearingDate);
                caseDetails.getData().put("gatekeepingOrderHearingDate2", hearingDate);
            });

        return respond(caseDetails);
    }

    @PostMapping("/prepare-decision/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleGenerateDraftMidEvent(@RequestBody CallbackRequest request) {
        final CaseData caseData = orderService.updateStandardDirections(request.getCaseDetails());
        final CaseDetailsMap data = caseDetailsMap(request.getCaseDetails());

        //TODO: need to work out if this callback is needed for urgent directions
//        if (Objects.nonNull(caseData.getUrgentDirectionsRouter())) {
//            throw new UnsupportedOperationException("The prepare-decision callback does not support urgent route");
//        }

        data.put("gatekeepingOrderSealDecision", orderService.buildSealDecision(caseData));

        if (caseData.getGatekeepingOrderRouter() == SERVICE) {
            data.put("standardDirections", caseData.getGatekeepingOrderEventData().getStandardDirections());
        }
        return respond(data);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {

        final CaseData caseData = orderService.updateStandardDirections(request.getCaseDetails());
        final CaseDetailsMap data = caseDetailsMap(request.getCaseDetails());

        final GatekeepingOrderSealDecision decision = caseData.getGatekeepingOrderEventData()
            .getGatekeepingOrderSealDecision();

        final UrgentDirectionsRoute sdoRouter = caseData.getUrgentDirectionsRouter();

        switch (sdoRouter) {
//            case URGENT:
//                data.putAll(urgentOrderService.finalise(caseData));
//                break;
            case UPLOAD:
                data.put("standardDirectionOrder", orderService.buildOrderFromUploadedFile(caseData));
                break;
            case SERVICE:
                data.put("standardDirectionOrder", orderService.buildOrderFromGeneratedFile(caseData));
                break;
        }

//        if (URGENT == sdoRouter || decision.isSealed()) {
//            data.put("state", CASE_MANAGEMENT);
//            if (GATEKEEPING == caseData.getState()) {
//                request.getCaseDetails().setData(data);
//                List<DocmosisTemplates> nopTemplates = orderService.getNoticeOfProceedingsTemplates(caseData);
//                data.put("noticeOfProceedingsBundle",
//                    nopService.uploadNoticesOfProceedings(getCaseData(request.getCaseDetails()), nopTemplates));
//            }
//        }

        removeTemporaryFields(data,
            "urgentHearingOrderDocument",
            "urgentHearingAllocation",
            "showUrgentHearingAllocation",
            "currentSDO",
            "preparedSDO",
            "replacementSDO",
            "useServiceRoute",
            "useUploadRoute",
            "judgeAndLegalAdvisor",
            "gatekeepingOrderHearingDate1",
            "gatekeepingOrderHearingDate2",
            "gatekeepingOrderHasHearing1",
            "gatekeepingOrderHasHearing2"
        );

//        if (decision.isSealed() || sdoRouter == URGENT) {
//            removeTemporaryFields(data, "gatekeepingOrderIssuingJudge", "customDirections");
//        }

        //TODO: find out how to populate the allocation decision, since we've removed that question
//        final Allocation allocationDecision = allocationService.createAllocationDecisionIfNull(getCaseData(request));
//        data.put("allocationDecision", allocationDecision);

        return respond(data);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest request) {
        CaseData caseData = getCaseData(request);
        final CaseDetails caseDetails = request.getCaseDetails();
        final Map<String, Object> data = caseDetails.getData();

        final GatekeepingOrderRoute sdoRouter = caseData.getGatekeepingOrderRouter();

        Map<String, Object> updates = new HashMap<>();
        switch (sdoRouter) {
//            case URGENT:
//                updates.putAll(urgentOrderService.sealDocumentAfterEventSubmitted(caseData));
//                break;
            case UPLOAD:
                updates.put("standardDirectionOrder", orderService.sealDocumentAfterEventSubmitted(caseData));
                break;
        }

        final CaseData caseDataAfterSealing;
        if (updates.isEmpty()) {
            caseDataAfterSealing = caseData;
        } else {
            data.putAll(updates);
            caseDataAfterSealing = getCaseData(caseDetails);
        }

        coreCaseDataService.triggerEvent(caseDataAfterSealing.getId(),
            "internal-change-add-gatekeeping",
            updates);

        CaseData caseDataBefore = getCaseDataBefore(request);

        notificationDecider.buildEventToPublish(caseDataAfterSealing, caseDataBefore.getState())
            .ifPresent(eventToPublish -> {
                coreCaseDataService.triggerEvent(
                    JURISDICTION,
                    CASE_TYPE,
                    caseDataAfterSealing.getId(),
                    "internal-change-SEND_DOCUMENT",
                    Map.of("documentToBeSent", eventToPublish.getOrder()));

                publishEvent(eventToPublish);
            });
    }


    @PostMapping("/post-submit-callback/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handlePostSubmittedEvent(@RequestBody CallbackRequest request) {
        final CaseData caseData = getCaseData(request);
        final CaseDetails caseDetails = request.getCaseDetails();
        final GatekeepingOrderSealDecision decision = caseData.getGatekeepingOrderEventData()
            .getGatekeepingOrderSealDecision();
        removeTemporaryFields(caseDetails, "gatekeepingOrderSealDecision");

        final GatekeepingOrderRoute sdoRouter = caseData.getGatekeepingOrderRouter();
//        if (decision.isSealed() || sdoRouter == URGENT) {
//            removeTemporaryFields(caseDetails, "gatekeepingOrderRouter");
//        }

        return respond(caseDetails);
    }
}

