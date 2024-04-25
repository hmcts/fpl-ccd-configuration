package uk.gov.hmcts.reform.fpl.controllers;

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
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderDataFixer;
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderRouteValidator;
import uk.gov.hmcts.reform.fpl.service.sdo.ListAdminEventNotificationDecider;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.allNotNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.Event.ADD_URGENT_DIRECTIONS;
import static uk.gov.hmcts.reform.fpl.enums.Event.JUDICIAL_GATEKEEPNIG;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.UPLOAD;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@RestController
@RequestMapping("/callback/add-gatekeeping-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddGatekeepingOrderController extends CallbackController {

    private final GatekeepingOrderService orderService;

    private final CourtLevelAllocationService allocationService;
    private final GatekeepingOrderRouteValidator routeValidator;
    private final ListAdminEventNotificationDecider notificationDecider;
    private final GatekeepingOrderDataFixer dataFixer;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        removeTemporaryFields(callbackRequest.getCaseDetails(),
            "gatekeepingOrderRouter", "urgentDirectionsRouter");
        final CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        final String eventName = callbackRequest.getEventId();
        final List<String> errors = routeValidator.allowAccessToEvent(caseData, eventName);
        final CaseDetailsMap data = dataFixer.addLanguageRequirement(caseDetailsMap(callbackRequest.getCaseDetails()));

        if (isNotEmpty(errors)) {
            return respond(data, errors);
        }

        final GatekeepingOrderRoute draftOrderRoute;
        final StandardDirectionOrder draftOrder;

        if (JUDICIAL_GATEKEEPNIG.getId().equals(eventName)) {
            draftOrderRoute = caseData.getGatekeepingOrderRouter();
            draftOrder = caseData.getStandardDirectionOrder();
        } else {
            draftOrderRoute = caseData.getUrgentDirectionsRouter();
            draftOrder = caseData.getUrgentDirectionsOrder();
        }

        if (allNotNull(draftOrderRoute, draftOrder)) {
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

        if (!ADD_URGENT_DIRECTIONS.getId().equals(eventName)) {
            final Allocation allocationDecision = allocationService.createDecision(caseData);
            data.put("allocationDecision", allocationDecision);
        }

        return respond(data);
    }

    @PostMapping("/direction-selection/mid-event")
    public AboutToStartOrSubmitCallbackResponse populateSelectedDirections(@RequestBody CallbackRequest request) {
        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        if (caseData.getGatekeepingOrderRouter() == UPLOAD || caseData.getUrgentDirectionsRouter() == UPLOAD) {
            throw new UnsupportedOperationException(
                "The direction-selection callback does not support the UPLOAD route"
            );
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

        if (caseData.getGatekeepingOrderRouter() == SERVICE || caseData.getUrgentDirectionsRouter() == SERVICE) {
            data.put("standardDirections", caseData.getGatekeepingOrderEventData().getStandardDirections());
        }
        return respond(data);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest request) {

        final CaseData caseData = orderService.updateStandardDirections(request.getCaseDetails());
        final CaseDetailsMap data = caseDetailsMap(request.getCaseDetails());

        final GatekeepingOrderRoute sdoRouter;
        final String orderType;
        if (Objects.nonNull(caseData.getGatekeepingOrderRouter())) {
            sdoRouter = caseData.getGatekeepingOrderRouter();
            orderType = "standardDirectionOrder";
        } else {
            sdoRouter = caseData.getUrgentDirectionsRouter();
            orderType = "urgentDirectionsOrder";
        }

        switch (sdoRouter) {
            case UPLOAD:
                data.put(orderType, orderService.buildOrderFromUploadedFile(caseData));
                break;
            case SERVICE:
                data.put(orderType, orderService.buildOrderFromGeneratedFile(caseData));
                break;
        }

        if (Objects.isNull(caseData.getUrgentDirectionsRouter())) {
            final Allocation allocationDecision =
                allocationService.createAllocationDecisionIfNull(getCaseData(request));
            data.put("allocationDecision", allocationDecision);
        }

        return respond(data);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest request) {
        CaseData caseData = getCaseData(request);
        notificationDecider.buildEventToPublish(caseData)
            .ifPresent(this::publishEvent);
    }
}
