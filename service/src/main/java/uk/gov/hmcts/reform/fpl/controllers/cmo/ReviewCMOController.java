package uk.gov.hmcts.reform.fpl.controllers.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderIssuedEvent;
import uk.gov.hmcts.reform.fpl.events.CaseManagementOrderRejectedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.cmo.ReviewCMOService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.RETURNED;

@Api
@RestController
@RequestMapping("/callback/review-cmo")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewCMOController {

    private final ObjectMapper mapper;
    private final ReviewCMOService reviewCMOService;
    private final DocumentSealingService documentSealingService;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<CaseManagementOrder>> cmosReadyForApproval = reviewCMOService.getCMOsReadyForApproval(
            caseData.getDraftUploadedCMOs());

        data.remove("reviewCMODecision");

        data.putAll(reviewCMOService.handlePageDisplayLogic(cmosReadyForApproval));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        Element<CaseManagementOrder> selectedCMO = reviewCMOService.getSelectedCMO(caseData);

        data.put("reviewCMODecision", ReviewDecision.builder()
            .hearing(selectedCMO.getValue().getHearing())
            .document(selectedCMO.getValue().getOrder())
            .build());

        List<Element<CaseManagementOrder>> cmosReadyForApproval = reviewCMOService.getCMOsReadyForApproval(
            caseData.getDraftUploadedCMOs());

        if (!(caseData.getCmoToReviewList() instanceof DynamicList)) {
            // reconstruct dynamic list
            data.put("cmoToReviewList", reviewCMOService.buildDynamicList(cmosReadyForApproval, selectedCMO.getId()));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest)
        throws Exception {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        Map<String, Object> data = caseDetails.getData();
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<Element<CaseManagementOrder>> cmosReadyForApproval = reviewCMOService.getCMOsReadyForApproval(
            caseData.getDraftUploadedCMOs());

        if (!cmosReadyForApproval.isEmpty()) {
            Element<CaseManagementOrder> cmo = reviewCMOService.getSelectedCMO(caseData);

            if (SEND_TO_ALL_PARTIES.equals(caseData.getReviewCMODecision().getDecision())) {
                caseData.getDraftUploadedCMOs().remove(cmo);

                Element<CaseManagementOrder> cmoToSeal = reviewCMOService.getCMOToSeal(caseData, cmo);

                DocumentReference sealedDocument = documentSealingService.sealDocument(cmoToSeal.getValue().getOrder());
                cmoToSeal.getValue().setOrder(sealedDocument);

                List<Element<CaseManagementOrder>> sealedCMOs = caseData.getSealedCMOs();
                sealedCMOs.add(cmoToSeal);

                data.put("sealedCMOs", sealedCMOs);
            } else {
                cmo.getValue().setStatus(RETURNED);
                cmo.getValue().setRequestedChanges(caseData.getReviewCMODecision().getChangesRequestedByJudge());
            }

            data.put("draftUploadedCMOs", caseData.getDraftUploadedCMOs());
            data.remove("numDraftCMOs");
            data.remove("cmoToReviewList");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @PostMapping("/submitted")
    public void handleSubmitted(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseDataBefore = mapper.convertValue(callbackRequest.getCaseDetailsBefore().getData(), CaseData.class);
        CaseData caseData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);

        List<Element<CaseManagementOrder>> cmosReadyForApproval = reviewCMOService.getCMOsReadyForApproval(
            caseDataBefore.getDraftUploadedCMOs());

        if (!cmosReadyForApproval.isEmpty()) {
            if (SEND_TO_ALL_PARTIES.equals(caseData.getReviewCMODecision().getDecision())) {
                CaseManagementOrder sealed = reviewCMOService.getLatestSealedCMO(caseData.getSealedCMOs());

                eventPublisher.publishEvent(new CaseManagementOrderIssuedEvent(callbackRequest, sealed));
            } else {
                List<Element<CaseManagementOrder>> draftCMOsBefore = caseDataBefore.getDraftUploadedCMOs();
                List<Element<CaseManagementOrder>> draftCMOs = caseData.getDraftUploadedCMOs();

                //Get the CMO that was modified (status changed from READY -> RETURNED)
                draftCMOs.removeAll(draftCMOsBefore);
                CaseManagementOrder cmoToReturn = draftCMOs.get(0).getValue();

                eventPublisher.publishEvent(new CaseManagementOrderRejectedEvent(callbackRequest, cmoToReturn));
            }
        }
    }
}
