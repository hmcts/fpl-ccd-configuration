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
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.GatekeepingOrderSealDecision;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.GatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfProceedingsService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderEventNotificationDecider;
import uk.gov.hmcts.reform.fpl.service.sdo.GatekeepingOrderRouteValidator;
import uk.gov.hmcts.reform.fpl.service.sdo.UrgentGatekeepingOrderService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.allNotNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.State.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.State.GATEKEEPING;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.GatekeepingOrderRoute.URGENT;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper.removeTemporaryFields;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Api
@RestController
@RequestMapping("/callback/add-gatekeeping-order")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddGatekeepingOrderController extends CallbackController {

    private final CaseConverter caseConverter;
    private final GatekeepingOrderService orderService;
    private final NoticeOfProceedingsService nopService;
    private final CoreCaseDataService coreCaseDataService;
    private final GatekeepingOrderRouteValidator routeValidator;
    private final UrgentGatekeepingOrderService urgentOrderService;
    private final GatekeepingOrderEventNotificationDecider notificationDecider;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        final CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        final CaseDetailsMap data = caseDetailsMap(callbackRequest.getCaseDetails());
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

        return respond(data);
    }

    @PostMapping("/pre-populate/mid-event")
    public CallbackResponse handleMidEventPrePopulation(@RequestBody CallbackRequest request) {
        final CaseDetails caseDetails = request.getCaseDetails();
        final CaseData caseData = getCaseData(caseDetails);

        if (caseData.getGatekeepingOrderRouter() == URGENT) {
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

        if (caseData.getGatekeepingOrderRouter() == URGENT) {
            throw new UnsupportedOperationException("The prepare-decision callback does not support urgent route");
        }

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

        final GatekeepingOrderRoute sdoRouter = caseData.getGatekeepingOrderRouter();

        switch (sdoRouter) {
            case URGENT:
                data.putAll(urgentOrderService.finalise(caseData));
                break;
            case UPLOAD:
                data.put("standardDirectionOrder", orderService.buildOrderFromUploadedFile(caseData));
                break;
            case SERVICE:
                data.put("standardDirectionOrder", orderService.buildOrderFromGeneratedFile(caseData));
                break;
        }

        if (URGENT == sdoRouter || decision.isSealed()) {
            data.put("state", CASE_MANAGEMENT);

            if (GATEKEEPING == caseData.getState()) {
                List<DocmosisTemplates> nopTemplates = orderService.getNoticeOfProceedingsTemplates(caseData);
                data.put("noticeOfProceedingsBundle", nopService.uploadNoticesOfProceedings(caseData, nopTemplates));
            }
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
            "gatekeepingOrderSealDecision",
            "gatekeepingOrderHearingDate1",
            "gatekeepingOrderHearingDate2",
            "gatekeepingOrderHasHearing1",
            "gatekeepingOrderHasHearing2"
        );

        if (decision.isSealed()) {
            removeTemporaryFields(data, "gatekeepingOrderIssuingJudge", "gatekeepingOrderRouter", "customDirections");
        }

        return respond(data);
    }

    @PostMapping("/submitted")
    public void handleSubmittedEvent(@RequestBody CallbackRequest request) {
        CaseData caseData = getCaseData(request);
        CaseData caseDataBefore = getCaseDataBefore(request);

        notificationDecider.buildEventToPublish(caseData, caseDataBefore.getState())
            .ifPresent(eventToPublish -> {
                coreCaseDataService.triggerEvent(
                    JURISDICTION,
                    CASE_TYPE,
                    caseData.getId(),
                    "internal-change-SEND_DOCUMENT",
                    Map.of("documentToBeSent", eventToPublish.getOrder()));

                publishEvent(eventToPublish);
            });
    }
}
