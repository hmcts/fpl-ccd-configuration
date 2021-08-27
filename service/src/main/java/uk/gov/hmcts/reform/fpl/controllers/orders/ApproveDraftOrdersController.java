package uk.gov.hmcts.reform.fpl.controllers.orders;

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
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.events.AfterSubmissionCaseDataUpdated;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.cmo.ApproveDraftOrdersService;
import uk.gov.hmcts.reform.fpl.service.cmo.DraftOrdersEventNotificationBuilder;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData.reviewDecisionFields;
import static uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData.transientFields;
import static uk.gov.hmcts.reform.fpl.model.order.selector.Selector.newSelector;

@Api
@RestController
@RequestMapping("/callback/approve-draft-orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApproveDraftOrdersController extends CallbackController {

    private final ApproveDraftOrdersService approveDraftOrdersService;
    private final DraftOrdersEventNotificationBuilder draftOrdersEventNotificationBuilder;
    private final OthersService othersService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        CaseDetailsHelper.removeTemporaryFields(caseDetails, reviewDecisionFields());

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

        if (isEmpty(errors) && isNotEmpty(caseData.getAllOthers())) {
            caseDetails.getData().put("hasOthers", "Yes");
            caseDetails.getData().put("others_label", othersService.getOthersLabel(caseData.getAllOthers()));
            caseDetails.getData().put("othersSelector", newSelector(caseData.getAllOthers().size()));

            if (approveDraftOrdersService.hasApprovedReviewDecision(caseData, data)) {
                caseDetails.getData().put("reviewCMOShowOthers", "Yes");
            }
        }

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
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

            caseDetails.getData().put("lastHearingOrderDraftsHearingId",
                selectedOrdersBundle.getValue().getHearingId());
        }

        CaseDetailsHelper.removeTemporaryFields(caseDetails, transientFields());

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        publishEvent(new AfterSubmissionCaseDataUpdated(caseData, getCaseDataBefore(callbackRequest)));

        draftOrdersEventNotificationBuilder.buildEventsToPublish(caseData).forEach(this::publishEvent);
    }
}
