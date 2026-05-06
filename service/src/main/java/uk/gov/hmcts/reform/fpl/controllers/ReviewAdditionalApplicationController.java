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
import uk.gov.hmcts.reform.fpl.exceptions.HearingOrdersBundleNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;
import uk.gov.hmcts.reform.fpl.model.order.DraftOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ReviewAdditionalApplicationService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.cmo.ApproveDraftOrdersService;
import uk.gov.hmcts.reform.fpl.service.markdown.ReviewAdditionalApplicationMarkdownService;


import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Slf4j
@RestController
@RequestMapping("/callback/review-additional-application")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewAdditionalApplicationController extends CallbackController {

    private final ApproveDraftOrdersService approveDraftOrdersService;
    private final CoreCaseDataService coreCaseDataService;
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

        return respond(caseDetails);
    }

    @PostMapping("/submitted")
    public SubmittedCallbackResponse handleSubmittedEvent(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails oldCaseDetails = callbackRequest.getCaseDetails();

        CaseDetails caseDetails = coreCaseDataService.performPostSubmitCallbackWithoutChange(oldCaseDetails.getId(),
            "internal-change-approve-add-apps");

        if (isEmpty(caseDetails)) {
            // if our callback has failed 3 times, all we have is the prior caseData to send notifications based on
            caseDetails = oldCaseDetails;
        }

        CaseData caseData = getCaseData(caseDetails);

        MarkdownData markdownData = markdownService.getMarkdownData(caseData.getCaseName());

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(markdownData.getHeader())
            .confirmationBody(markdownData.getBody())
            .build();
    }

    @PostMapping("/post-submit-callback/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse postHandleAboutToSubmitEvent(
        @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> data = caseDetails.getData();

        ConfirmApplicationReviewedEventData eventData = caseData.getConfirmApplicationReviewedEventData();
        C2DocumentBundle bundle  = eventData.getC2AdditionalApplicationToBeReview().toC2DocumentBundle();

        Element<DraftOrder> draftOrder = bundle.getDraftOrdersBundle().getFirst();

        Element<HearingOrdersBundle> bundleFromDraftOrder = caseData.getHearingOrdersBundlesDrafts().stream()
        .filter(bundleElement ->
            bundleElement.getValue().getOrders().stream()
                .anyMatch(orderElement -> orderElement.getId().equals(draftOrder.getId()))
        )
        .findFirst()
        .orElseThrow(() -> new HearingOrdersBundleNotFoundException(
            "No HearingOrdersBundle found containing order with element id: " + draftOrder.getId()
        ));

        switch (caseData.getApproveAdditionalAppRouter()) {
            case APPROVE_APPLICATION_AND_ORDER: {
                ReviewDecision reviewDecision = ReviewDecision.builder()
                    .decision(SEND_TO_ALL_PARTIES)
                    .build();
                approveDraftOrdersService.approveAndSealDraftOrder(
                    caseData,
                    data,
                    bundleFromDraftOrder,
                    draftOrder.getId(),
                    reviewDecision
                );
                caseDetails.getData().put("orderCollection", data.get("orderCollection"));
                caseDetails.getData().putAll(
                    approveDraftOrdersService.updateHearingDraftOrdersBundle(caseData, bundleFromDraftOrder)
                );
                break;
            }
            default:
                break;
        }

        // Clean up temp fields here as needed in post-submit and /submitted returns SubmittedCallbackResponse
        ConfirmApplicationReviewedEventData.eventFields().forEach(caseDetails.getData()::remove);

        return respond(caseDetails);
    }

}
