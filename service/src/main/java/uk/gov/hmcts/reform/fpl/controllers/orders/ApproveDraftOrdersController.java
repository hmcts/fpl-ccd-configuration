package uk.gov.hmcts.reform.fpl.controllers.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.cmo.ApproveDraftOrdersService;
import uk.gov.hmcts.reform.fpl.service.cmo.DraftOrdersEventNotificationBuilder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData.reviewDecisionFields;
import static uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData.transientFields;

@RestController
@RequestMapping("/callback/approve-draft-orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApproveDraftOrdersController extends CallbackController {

    private final ApproveDraftOrdersService approveDraftOrdersService;
    private final DraftOrdersEventNotificationBuilder draftOrdersEventNotificationBuilder;
    private final CoreCaseDataService coreCaseDataService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        CaseDetailsHelper.removeTemporaryFields(caseDetails, reviewDecisionFields());
        CaseDetailsHelper.removeTemporaryFields(caseDetails, "orderReviewUrgency");

        caseDetails.getData().putAll(approveDraftOrdersService.getPageDisplayControls(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        CaseDetailsHelper.removeTemporaryFields(caseDetails, reviewDecisionFields());

        caseDetails.getData().putAll(approveDraftOrdersService.populateDraftOrdersData(caseData));

        if (!(caseData.getCmoToReviewList() instanceof DynamicList)) {
            // reconstruct dynamic list
            caseDetails.getData().put("cmoToReviewList", approveDraftOrdersService.buildDynamicList(caseData));
        }

        return respond(caseDetails);
    }

    @PostMapping("/validate-review-decision/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateReviewDecision(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        List<String> errors = approveDraftOrdersService.validateDraftOrdersReviewDecision(caseData, data);

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        // DFPL-1171 move all document processing step to post-about-to-submitted stage

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails oldCaseDetails = callbackRequest.getCaseDetails();

        // Start event with concurrency controls
        CaseDetails caseDetails = coreCaseDataService.performPostSubmitCallbackWithoutChange(oldCaseDetails.getId(),
            "internal-change-approve-order");

        if (isEmpty(caseDetails)) {
            // if our callback has failed 3 times, all we have is the prior caseData to send notifications based on
            caseDetails = oldCaseDetails;
        }

        CaseData caseData = getCaseData(caseDetails);

        publishEvent(new AfterSubmissionCaseDataUpdated(caseData, getCaseDataBefore(callbackRequest)));
    }

    @PostMapping("/post-submit-callback/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse postHandleAboutToSubmitEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        // reset ordersToBeSent in the notifications
        caseDetails.getData().remove("ordersToBeSent");

        if (isNotEmpty(caseData.getBundlesForApproval())) {

            Element<HearingOrdersBundle> selectedOrdersBundle =
                approveDraftOrdersService.getSelectedHearingDraftOrdersBundle(caseData);

            // review cmo
            data.putAll(approveDraftOrdersService.reviewCMO(caseData, selectedOrdersBundle));

            // review C21 orders
            approveDraftOrdersService.reviewC21Orders(caseData, data, selectedOrdersBundle);

            // update list of rejected orders
            approveDraftOrdersService.updateRejectedHearingOrders(data);

            caseDetails.getData().put("lastHearingOrderDraftsHearingId",
                selectedOrdersBundle.getValue().getHearingId());
        }

        CaseDetailsHelper.removeTemporaryFields(caseDetails, transientFields());

        return respond(caseDetails);
    }

    @PostMapping("/post-submit-callback/submitted")
    public void handlePostSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        draftOrdersEventNotificationBuilder.buildEventsToPublish(caseData).forEach(this::publishEvent);
    }
}
