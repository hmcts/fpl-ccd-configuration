package uk.gov.hmcts.reform.fpl.controllers;

import io.swagger.annotations.Api;
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
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ConfirmApplicationReviewedService;

@Api
@Slf4j
@RestController
@RequestMapping("/callback/confirm-additional-application-reviewed")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfirmApplicationReviewedController extends CallbackController {

    private final ConfirmApplicationReviewedService confirmApplicationReviewedService;

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().putAll(confirmApplicationReviewedService.initEventField(caseData));

        return respond(caseDetails);
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("additionalApplicationsBundleToBeReviewed",
            confirmApplicationReviewedService.getSelectedApplicationsToBeReviewed(caseData).getValue());

        return respond(caseDetails);
    }


    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse validateReplyMessage(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        caseDetails.getData().put("additionalApplicationsBundle",
            confirmApplicationReviewedService.markSelectedBundleAsReviewed(caseData));

        return respond(caseDetails);
    }
}
