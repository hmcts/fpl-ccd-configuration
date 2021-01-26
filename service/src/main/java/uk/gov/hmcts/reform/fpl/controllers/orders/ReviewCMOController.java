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
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.cmo.ReviewCMOService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;

@Api
@RestController
@RequestMapping("/callback/review-cmo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewCMOController extends CallbackController {

    private final ReviewCMOService reviewCMOService;
    private final CoreCaseDataService coreCaseDataService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        //TODO: move data fields to event data class
        resetReviewDecisionData(caseDetails);

        caseDetails.getData().putAll(reviewCMOService.getPageDisplayControls(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(reviewCMOService.populateDraftOrdersData(caseData));

        if (!(caseData.getCmoToReviewList() instanceof DynamicList)) {
            // reconstruct dynamic list
            caseDetails.getData().put("cmoToReviewList", reviewCMOService.buildDynamicList(caseData));
        }

        return respond(caseDetails);
    }

    @PostMapping("/validate-review-decision/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateReviewDecision(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        List<String> errors = reviewCMOService.isReviewDecisionValid(caseData, data);

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        Element<HearingOrdersBundle> selectedOrdersBundle =
            reviewCMOService.getSelectedHearingDraftOrdersBundle(caseData);

        // review cmo
        data.putAll(reviewCMOService.reviewCMO(caseData, selectedOrdersBundle));

        // review C21 orders
        reviewCMOService.reviewC21Orders(caseData, data, selectedOrdersBundle);

        removeTransientFields(caseDetails);

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);
        CaseData caseData = getCaseData(callbackRequest);

        //Checks caseDataBefore as caseData has been modified by this point
        List<Element<HearingOrder>> cmosReadyForApproval = reviewCMOService.getCMOsReadyForApproval(
            caseDataBefore);

        if (!cmosReadyForApproval.isEmpty() && caseData.getReviewCMODecision() != null) {
            if (!JUDGE_REQUESTED_CHANGES.equals(caseData.getReviewCMODecision().getDecision())) {
                HearingOrder sealed = reviewCMOService.getLatestSealedCMO(caseData);
                DocumentReference documentToBeSent = sealed.getOrder();

                coreCaseDataService.triggerEvent(
                    callbackRequest.getCaseDetails().getJurisdiction(),
                    callbackRequest.getCaseDetails().getCaseTypeId(),
                    callbackRequest.getCaseDetails().getId(),
                    "internal-change-SEND_DOCUMENT",
                    Map.of("documentToBeSent", documentToBeSent)
                );

                publishEvent(new CaseManagementOrderIssuedEvent(caseData, sealed));
            } else {
                List<Element<HearingOrder>> draftCMOsBefore = caseDataBefore.getDraftUploadedCMOs();
                List<Element<HearingOrder>> draftCMOs = caseData.getDraftUploadedCMOs();

                //Get the CMO that was modified (status changed from READY -> RETURNED)
                draftCMOs.removeAll(draftCMOsBefore);
                HearingOrder cmoToReturn = draftCMOs.get(0).getValue();

                publishEvent(new CaseManagementOrderRejectedEvent(caseData, cmoToReturn));
            }
        }
    }

    private void removeTransientFields(CaseDetails caseDetails) {
        CaseDetailsHelper.removeTemporaryFields(caseDetails, "numDraftCMOs", "cmoToReviewList",
            "draftCMOExists", "draftBlankOrdersCount", "cmoDraftOrderTitle",
            "draftOrder1Title", "draftOrder2Title", "draftOrder3Title", "draftOrder4Title", "draftOrder5Title",
            "draftOrder6Title", "draftOrder7Title", "draftOrder8Title", "draftOrder9Title", "draftOrder10Title",
            "cmoDraftOrderDocument", "draftOrder1Document", "draftOrder2Document", "draftOrder3Document",
            "draftOrder4Document", "draftOrder5Document", "draftOrder6Document", "draftOrder7Document",
            "draftOrder8Document", "draftOrder9Document", "draftOrder10Document");
    }

    private void resetReviewDecisionData(CaseDetails caseDetails) {
        CaseDetailsHelper.removeTemporaryFields(caseDetails, "reviewCMODecision", "reviewDecision1",
            "reviewDecision2", "reviewDecision3", "reviewDecision4", "reviewDecision5", "reviewDecision6",
            "reviewDecision7", "reviewDecision8", "reviewDecision9", "reviewDecision10");
    }
}
