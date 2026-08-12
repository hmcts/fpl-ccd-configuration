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
import uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions;
import uk.gov.hmcts.reform.fpl.exceptions.HearingOrdersBundleNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.model.markdown.MarkdownData;
import uk.gov.hmcts.reform.fpl.model.order.DraftOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ReviewAdditionalApplicationService;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ApplicationRefusalOrderService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.service.additionalapplications.ApplicationRefusalOrderService;
import uk.gov.hmcts.reform.fpl.service.cmo.ApproveDraftOrdersService;
import uk.gov.hmcts.reform.fpl.service.cmo.HearingOrderGenerator;
import uk.gov.hmcts.reform.fpl.service.markdown.ReviewAdditionalApplicationMarkdownService;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
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
    private final HearingOrderGenerator hearingOrderGenerator;
    private final ApplicationRefusalOrderService refusalOrderService;

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
    public AboutToStartOrSubmitCallbackResponse handleAdditionalApplication(@RequestBody CallbackRequest
                                                                                    callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        ConfirmApplicationReviewedEventData eventData = caseData.getConfirmApplicationReviewedEventData();
        List<String> errors = new ArrayList<>();

        switch (caseData.getApproveAdditionalAppRouter()) {
            case APPROVE_APPLICATION_AND_ORDER, APPROVE_APPLICATION_CHANGE_ORDER:
                caseDetails.getData().put("reviewOrderUrgency", YES);
                caseDetails.getData().put("addCoverSheet", YES);

                C2DocumentBundle bundle  = eventData.getC2AdditionalApplicationToBeReview().toC2DocumentBundle();

                Element<DraftOrder> draftOrder = bundle.getDraftOrdersBundle().getFirst();
                DocumentReference amendedDraftOrderDocument = isEmpty(eventData.getAmendedDraftOrder())
                    ? null : eventData.getAmendedDraftOrder();

                DocumentReference previewOrder = hearingOrderGenerator.addCoverSheet(caseData
                        .toBuilder().reviewDraftOrdersData(caseData.getReviewDraftOrdersData().toBuilder()
                            .judgeTitleAndName(approveDraftOrdersService
                                .getJudgeTitleAndNameOfCurrentUser(caseData))
                            .build())
                        .build(),
                    isEmpty(eventData.getAmendedDraftOrder())
                        ? draftOrder.getValue().getDocument() : amendedDraftOrderDocument);

                caseDetails.getData().put("previewApprovedOrder1", previewOrder);
                caseDetails.getData().put("previewApprovedOrderTitle1", String.format("Order %s",
                    draftOrder.getValue().getTitle()));
                break;
            case APPLICANT_CHANGE_ORDER:
                caseDetails.getData().put("reviewOrderUrgency", NO);
                caseDetails.getData().put("addCoverSheet", NO);
                break;
            case REFUSE:
                caseDetails.getData().put("reviewOrderUrgency", NO);
                caseDetails.getData().put("addCoverSheet", NO);

                caseDetails.getData().put("previewApprovedOrder1",
                    refusalOrderService.buildApplicationRefusalOrderDocument(caseData, eventData.getJudgeNameAndTitle(),
                        eventData.getC2AdditionalApplicationToBeReview().getUploadedDateTime(),
                        eventData.getReviewAdditionalAppRefusalReason(), false));
                break;
            case LIST:
                if (!reviewAdditionalApplicationService.hasFutureHearing(caseData)) {
                    errors.add("Cannot list application at next hearing because no future hearing exists");
                }
                caseDetails.getData().put("reviewOrderUrgency", NO);
                caseDetails.getData().put("addCoverSheet", NO);
                break;
            default:
                caseDetails.getData().put("reviewOrderUrgency", NO);
                caseDetails.getData().put("addCoverSheet", NO);
        }

        return respond(caseDetails, errors);
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
        CaseDetails oldCaseDetails = callbackRequest.getCaseDetails();

        approveDraftOrdersService.getJudgeTitleAndNameOfCurrentUser(getCaseData(oldCaseDetails));

        CaseDetails caseDetails = coreCaseDataService.performPostSubmitCallbackWithoutChange(oldCaseDetails.getId(),
            "internal-change-approve-add-apps");

        if (isEmpty(caseDetails)) {
            // if our callback has failed 3 times, all we have is the prior caseData to send notifications based on
            caseDetails = oldCaseDetails;
        }

        CaseData caseData = getCaseData(caseDetails);
        CaseData oldCaseData = getCaseData(oldCaseDetails);
        ConfirmApplicationReviewedEventData oldEventData = oldCaseData.getConfirmApplicationReviewedEventData();
        boolean isConfidential = YES.equals(oldEventData.getReviewAdditionalAppIsConfidential());
        ApproveAdditionalAppOptions selectedOption = oldCaseData.getApproveAdditionalAppRouter();

        MarkdownData markdownData = markdownService.getMarkdownData(caseData.getCaseName(),
            isConfidential,
            selectedOption);

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
        UUID draftOrderId = UUID.fromString(eventData.getReviewAdditionalAppDraftOrderId());
        boolean isConfidential = YES.equals(eventData.getReviewAdditionalAppIsConfidential());

        Element<HearingOrdersBundle> bundleFromDraftOrder = caseData.getHearingOrdersBundlesDrafts().stream()
            .filter(bundleElement -> {
                if (isConfidential) {
                    return bundleElement.getValue().getAllConfidentialOrders().stream()
                        .anyMatch(orderElement -> orderElement.getId().equals(draftOrderId));
                } else {
                    return bundleElement.getValue().getOrders().stream()
                        .anyMatch(orderElement -> orderElement.getId().equals(draftOrderId));
                }
            })
            .findFirst()
            .orElseThrow(() -> new HearingOrdersBundleNotFoundException(
                "No HearingOrdersBundle found containing order with element id: " + draftOrderId
            ));

        switch (caseData.getApproveAdditionalAppRouter()) {
            case APPROVE_APPLICATION_AND_ORDER: {
                ReviewDecision reviewDecision = ReviewDecision.builder()
                    .decision(SEND_TO_ALL_PARTIES)
                    .build();
                approveDraftOrdersService.approveAndSealDraftOrder(
                    caseData.toBuilder().reviewDraftOrdersData(caseData.getReviewDraftOrdersData().toBuilder()
                        .judgeTitleAndName(eventData.getJudgeNameAndTitle())
                        .build()).build(),
                    data,
                    bundleFromDraftOrder,
                    draftOrderId,
                    reviewDecision
                );
                caseDetails.getData().put("orderCollection", data.get("orderCollection"));
                caseDetails.getData().putAll(
                    approveDraftOrdersService.updateHearingDraftOrdersBundle(caseData, bundleFromDraftOrder)
                );
                break;
            }
            case APPROVE_APPLICATION_CHANGE_ORDER: {
                ReviewDecision reviewDecision = ReviewDecision.builder()
                    .decision(JUDGE_AMENDS_DRAFT)
                    .build();
                approveDraftOrdersService.approveAndSealDraftOrder(
                    caseData.toBuilder().reviewDraftOrdersData(caseData.getReviewDraftOrdersData().toBuilder()
                        .judgeTitleAndName(eventData.getJudgeNameAndTitle())
                        .build()).build(),
                    data,
                    bundleFromDraftOrder,
                    draftOrderId,
                    reviewDecision,
                    eventData.getAmendedDraftOrder()
                );
                caseDetails.getData().put("orderCollection", data.get("orderCollection"));
                caseDetails.getData().putAll(
                    approveDraftOrdersService.updateHearingDraftOrdersBundle(caseData, bundleFromDraftOrder)
                );
                break;
            }
            case APPLICANT_CHANGE_ORDER:
                caseDetails.getData().putAll(reviewAdditionalApplicationService.returnDraftOrderToApplicant(
                    caseData,
                    bundleFromDraftOrder,
                    draftOrderId,
                    eventData.getReviewAdditionalAppRequestedChanges()
                ));
                break;
            case REFUSE: {
                caseDetails.getData().putAll(reviewAdditionalApplicationService.addRefusalOrders(
                    caseData,
                    bundleFromDraftOrder,
                    draftOrderId
                ));
                caseDetails.getData().putAll(
                    approveDraftOrdersService.updateHearingDraftOrdersBundle(caseData, bundleFromDraftOrder)
                );
                break;
            }
            case LIST:
                caseDetails.getData().putAll(reviewAdditionalApplicationService.listApplicationAtNextHearing(
                    caseData,
                    bundleFromDraftOrder,
                    draftOrderId,
                    eventData
                ));
                break;
            default:
                break;
        }

        ConfirmApplicationReviewedEventData.postSubmitEventFields().forEach(caseDetails.getData()::remove);

        return respond(caseDetails);
    }

}
