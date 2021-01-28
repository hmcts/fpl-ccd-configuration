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
import uk.gov.hmcts.reform.fpl.service.cmo.ReviewDraftOrdersService;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;
import static uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData.reviewDecisionFields;
import static uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData.transientFields;

@Api
@RestController
@RequestMapping("/callback/approve-draft-orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApproveDraftOrdersController extends CallbackController {

    private final ReviewDraftOrdersService reviewDraftOrdersService;
    private final CoreCaseDataService coreCaseDataService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        CaseDetailsHelper.removeTemporaryFields(caseDetails, reviewDecisionFields());

        caseDetails.getData().putAll(reviewDraftOrdersService.getPageDisplayControls(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        CaseDetailsHelper.removeTemporaryFields(caseDetails, reviewDecisionFields());

        caseDetails.getData().putAll(reviewDraftOrdersService.populateDraftOrdersData(caseData));

        if (!(caseData.getCmoToReviewList() instanceof DynamicList)) {
            // reconstruct dynamic list
            caseDetails.getData().put("cmoToReviewList", reviewDraftOrdersService.buildDynamicList(caseData));
        }

        return respond(caseDetails);
    }

    @PostMapping("/validate-review-decision/mid-event")
    public AboutToStartOrSubmitCallbackResponse validateReviewDecision(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        List<String> errors = reviewDraftOrdersService.validateDraftOrdersReviewDecision(caseData, data);

        return respond(caseDetails, errors);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        Element<HearingOrdersBundle> selectedOrdersBundle =
            reviewDraftOrdersService.getSelectedHearingDraftOrdersBundle(caseData);

        // review cmo
        data.putAll(reviewDraftOrdersService.reviewCMO(caseData, selectedOrdersBundle));

        // review C21 orders
        reviewDraftOrdersService.reviewC21Orders(caseData, data, selectedOrdersBundle);

        CaseDetailsHelper.removeTemporaryFields(caseDetails, transientFields());

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);
        CaseData caseData = getCaseData(callbackRequest);

        //Checks caseDataBefore as caseData has been modified by this point
        List<Element<HearingOrder>> cmosReadyForApproval = reviewDraftOrdersService.getCMOsReadyForApproval(
            caseDataBefore);

        if (!cmosReadyForApproval.isEmpty() && caseData.getReviewCMODecision() != null
            && caseData.getReviewCMODecision().getDecision() != null) {
            if (!JUDGE_REQUESTED_CHANGES.equals(caseData.getReviewCMODecision().getDecision())) {
                HearingOrder sealed = reviewDraftOrdersService.getLatestSealedCMO(caseData);
                if (sealed != null) {
                    DocumentReference documentToBeSent = sealed.getOrder();

                    coreCaseDataService.triggerEvent(
                        callbackRequest.getCaseDetails().getJurisdiction(),
                        callbackRequest.getCaseDetails().getCaseTypeId(),
                        callbackRequest.getCaseDetails().getId(),
                        "internal-change-SEND_DOCUMENT",
                        Map.of("documentToBeSent", documentToBeSent)
                    );

                    publishEvent(new CaseManagementOrderIssuedEvent(caseData, sealed));
                }
            } else {
                List<Element<HearingOrder>> draftCMOsBefore = caseDataBefore.getDraftUploadedCMOs();
                List<Element<HearingOrder>> draftCMOs = caseData.getDraftUploadedCMOs();

                //Get the CMO that was modified (status changed from READY -> RETURNED)
                draftCMOsBefore.removeAll(draftCMOs);
                HearingOrder cmoToReturn = draftCMOsBefore.get(0).getValue();

                publishEvent(new CaseManagementOrderRejectedEvent(caseData, cmoToReturn));
            }
        }
    }
}
