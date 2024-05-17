package uk.gov.hmcts.reform.fpl.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ReviewAdditionalApplicationService;

@Slf4j
@RestController
@RequestMapping("/callback/review-additional-application")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewAdditionalApplicationController extends CallbackController {

    private final ReviewAdditionalApplicationService reviewAdditionalApplicationService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(reviewAdditionalApplicationService.initEventField(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("additionalApplicationsBundleToBeReviewed",
            reviewAdditionalApplicationService.getSelectedApplicationsToBeReviewed(caseData).getValue());

        return respond(caseDetails);
    }


    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmitEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("additionalApplicationsBundle",
            reviewAdditionalApplicationService.markSelectedBundleAsReviewed(caseData));

        ConfirmApplicationReviewedEventData.eventFields().forEach(caseDetails.getData()::remove);

        return respond(caseDetails);
    }
}
