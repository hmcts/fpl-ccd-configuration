package uk.gov.hmcts.reform.fpl.controllers.cmo;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.cmo.ReviewCMOService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;

@Api
@RestController
@RequestMapping("/callback/review-cmo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewCMOController extends CallbackController {

    private final ReviewCMOService reviewCMOService;
    private final DocumentSealingService documentSealingService;
    private final ApplicationEventPublisher eventPublisher;
    private final CoreCaseDataService coreCaseDataService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().remove("reviewCMODecision");
        caseDetails.getData().putAll(reviewCMOService.getPageDisplayControls(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        Element<CaseManagementOrder> selectedCMO = reviewCMOService.getSelectedCMO(caseData);

        caseDetails.getData().put("reviewCMODecision", ReviewDecision.builder()
            .hearing(selectedCMO.getValue().getHearing())
            .document(selectedCMO.getValue().getOrder())
            .build());

        if (!(caseData.getCmoToReviewList() instanceof DynamicList)) {
            // reconstruct dynamic list
            caseDetails.getData().put("cmoToReviewList", reviewCMOService.buildDynamicList(caseData));
        }

        return respond(caseDetails);
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest)
        throws Exception {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        List<Element<CaseManagementOrder>> cmosReadyForApproval = reviewCMOService.getCMOsReadyForApproval(caseData);

        if (!cmosReadyForApproval.isEmpty()) {
            Element<CaseManagementOrder> cmo = reviewCMOService.getSelectedCMO(caseData);


            if (!JUDGE_REQUESTED_CHANGES.equals(caseData.getReviewCMODecision().getDecision())) {
                Element<CaseManagementOrder> cmoToSeal = reviewCMOService.getCMOToSeal(caseData);

                caseData.getDraftUploadedCMOs().remove(cmo);

                cmoToSeal.getValue().setOrder(documentSealingService.sealDocument(cmoToSeal.getValue().getOrder()));

                List<Element<CaseManagementOrder>> sealedCMOs = caseData.getSealedCMOs();
                sealedCMOs.add(cmoToSeal);

                caseDetails.getData().put("sealedCMOs", sealedCMOs);
                caseDetails.getData().put("state", reviewCMOService.getStateBasedOnNextHearing(caseData, cmo.getId()));
            } else {
                cmo.getValue().setStatus(RETURNED);
                cmo.getValue().setRequestedChanges(caseData.getReviewCMODecision().getChangesRequestedByJudge());
            }

            caseDetails.getData().put("draftUploadedCMOs", caseData.getDraftUploadedCMOs());
            caseDetails.getData().remove("numDraftCMOs");
            caseDetails.getData().remove("cmoToReviewList");
        }

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseDataBefore = getCaseDataBefore(callbackRequest);
        CaseData caseData = getCaseData(callbackRequest);

        //Checks caseDataBefore as caseData has been modified by this point
        List<Element<CaseManagementOrder>> cmosReadyForApproval = reviewCMOService.getCMOsReadyForApproval(
            caseDataBefore);

        if (!cmosReadyForApproval.isEmpty()) {
            if (!JUDGE_REQUESTED_CHANGES.equals(caseData.getReviewCMODecision().getDecision())) {
                CaseManagementOrder sealed = reviewCMOService.getLatestSealedCMO(caseData);
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
                List<Element<CaseManagementOrder>> draftCMOsBefore = caseDataBefore.getDraftUploadedCMOs();
                List<Element<CaseManagementOrder>> draftCMOs = caseData.getDraftUploadedCMOs();

                //Get the CMO that was modified (status changed from READY -> RETURNED)
                draftCMOs.removeAll(draftCMOsBefore);
                CaseManagementOrder cmoToReturn = draftCMOs.get(0).getValue();

                publishEvent(new CaseManagementOrderRejectedEvent(caseData, cmoToReturn));
            }
        }
    }
}
