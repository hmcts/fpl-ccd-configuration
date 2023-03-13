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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.CourtLevelAllocationService;
import uk.gov.hmcts.reform.fpl.service.GatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderDataFixer;
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderEventNotificationDecider;
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderRouteValidator;
import uk.gov.hmcts.reform.fpl.service.sdo.ListAdminEventNotificationDecider;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.allNotNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.UPLOAD;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Api
@RestController
@RequestMapping("/callback/add-gatekeeping-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddGatekeepingOrderController extends CallbackController {

    private final GatekeepingOrderService orderService;
    private final NoticeOfProceedingsService nopService;
    private final CoreCaseDataService coreCaseDataService;

    private final CourtLevelAllocationService allocationService;
    private final GatekeepingOrderRouteValidator routeValidator;
    private final GatekeepingOrderEventNotificationDecider gatekeepingOrderEventNotificationDecider;
    private final ListAdminEventNotificationDecider notificationDecider;
    private final GatekeepingOrderDataFixer dataFixer;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        final CaseDetailsMap data = dataFixer.addLanguageRequirement(caseDetailsMap(callbackRequest.getCaseDetails()));
        final StandardDirectionOrder draftOrder = caseData.getStandardDirectionOrder();

        final GatekeepingOrderRoute draftOrderRoute = caseData.getGatekeepingOrderRouter();

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
            data.put("gatekeepingOrderIssuingJudge", orderService.setAllocatedJudgeLabel(caseData.getAllocatedJudge(),
                caseData.getGatekeepingOrderEventData().getGatekeepingOrderIssuingJudge()));
        }

        final Allocation allocationDecision = allocationService.createDecision(caseData);
        data.put("allocationDecision", allocationDecision);

        return respond(data);
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

        final GatekeepingOrderRoute sdoRouter = caseData.getGatekeepingOrderRouter();

        switch (sdoRouter) {
            case UPLOAD:
                data.put("standardDirectionOrder", orderService.buildOrderFromUploadedFile(caseData));
                break;
            case SERVICE:
                data.put("standardDirectionOrder", orderService.buildOrderFromGeneratedFile(caseData));
                break;
        }

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

        final Allocation allocationDecision = allocationService.createAllocationDecisionIfNull(getCaseData(request));
        data.put("allocationDecision", allocationDecision);

        return respond(data);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest request) {
        CaseData caseData = getCaseData(request);
        final CaseDetails caseDetails = request.getCaseDetails();
        final Map<String, Object> data = caseDetails.getData();

        final GatekeepingOrderRoute sdoRouter = caseData.getGatekeepingOrderRouter();

        Map<String, Object> updates = new HashMap<>();

        if (sdoRouter == UPLOAD) {
            updates.put("standardDirectionOrder", orderService.sealDocumentAfterEventSubmitted(caseData));
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

        gatekeepingOrderEventNotificationDecider.buildEventToPublish(caseDataAfterSealing, caseDataBefore.getState())
            .ifPresent(eventToPublish -> {
                coreCaseDataService.triggerEvent(
                    JURISDICTION,
                    CASE_TYPE,
                    caseDataAfterSealing.getId(),
                    "internal-change-SEND_DOCUMENT",
                    Map.of("documentToBeSent", eventToPublish.getOrder()));

                publishEvent(eventToPublish);
            });

        notificationDecider.buildEventToPublish(caseData)
            .ifPresent(this::publishEvent);
    }


    @PostMapping("/post-submit-callback/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handlePostSubmittedEvent(@RequestBody CallbackRequest request) {
        final CaseDetails caseDetails = request.getCaseDetails();
        removeTemporaryFields(caseDetails, "gatekeepingOrderSealDecision");

        return respond(caseDetails);
    }
}
