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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ReviewAdditionalApplicationService;
import uk.gov.hmcts.reform.fpl.service.markdown.ReviewAdditionalApplicationMarkdownService;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Slf4j
@RestController
@RequestMapping("/callback/review-additional-application")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewAdditionalApplicationController extends CallbackController {

    private final ReviewAdditionalApplicationMarkdownService markdownService;
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

        caseDetails.getData().putAll(reviewAdditionalApplicationService
            .initReviewFieldsForSelectedBundle(reviewAdditionalApplicationService
                .getSelectedApplicationsToBeReviewed(caseData).getValue()));

        return respond(caseDetails);
    }

    @PostMapping("/edit-hearing/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleAdditionalApplication(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        switch (caseData.getApproveAdditionalAppRouter()) {
            case APPROVE_APPLICATION_AND_ORDER, APPROVE_APPLICATION_CHANGE_ORDER:
                caseDetails.getData().put("reviewOrderUrgency", YES);
                break;
            default:
                caseDetails.getData().put("reviewOrderUrgency", NO);
        }

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

    @PostMapping("/submitted")
    public SubmittedCallbackResponse handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest);

        MarkdownData markdownData = markdownService.getMarkdownData(caseData.getCaseName());

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(markdownData.getHeader())
            .confirmationBody(markdownData.getBody())
            .build();
    }
}
