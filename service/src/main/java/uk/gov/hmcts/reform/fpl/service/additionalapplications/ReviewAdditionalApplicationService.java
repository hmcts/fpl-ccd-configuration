package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.ApproveAdditionalAppOptions;
import uk.gov.hmcts.reform.fpl.events.cmo.C2ApplicationRejectedEvent;
import uk.gov.hmcts.reform.fpl.events.cmo.ReviewCMOEvent;
import uk.gov.hmcts.reform.fpl.exceptions.HearingOrdersBundleNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.C2AdditionalApplicationEventData;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.cmo.ApproveDraftOrdersService;
import uk.gov.hmcts.reform.fpl.service.cmo.HearingOrderGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.getIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ConfidentialOrderBundleUtils.addToConfidentialOrderBundle;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewAdditionalApplicationService {
    public static final String ONLY_ONE_APPLICATION = "onlyOneApplicationToBeReviewed";
    private static final String APPLICANT_CHANGES_REQUESTED = "Applicant needs to make changes to the order";

    private final ApproveDraftOrdersService approveDraftOrdersService;
    private final HearingOrderGenerator hearingOrderGenerator;
    private final ApplicationRefusalOrderService refusalOrderService;
    // private final DraftOrdersEventNotificationBuilder draftOrdersEventNotificationBuilder;

    public Map<String, Object> initEventField(CaseData caseData) {
        Map<String, Object> resultMap = new HashMap<>();

        final List<Element<AdditionalApplicationsBundle>> applicationsBundlesToBeReviewed =
            getApplicationsToBeReviewed(caseData);

        if (isEmpty(applicationsBundlesToBeReviewed)) {
            resultMap.put("hasApplicationToBeReviewed", NO);
            resultMap.put(ONLY_ONE_APPLICATION, NO);
        } else {
            resultMap.put("hasApplicationToBeReviewed", YES);
            if (applicationsBundlesToBeReviewed.size() > 1) {
                resultMap.put("additionalApplicationToBeReviewedList", asDynamicList(applicationsBundlesToBeReviewed,
                    AdditionalApplicationsBundle::toLabel));
                resultMap.put(ONLY_ONE_APPLICATION, NO);
            } else if (applicationsBundlesToBeReviewed.size() == 1) {
                resultMap.put(ONLY_ONE_APPLICATION, YES);
                resultMap.putAll(initReviewFieldsForSelectedBundle(applicationsBundlesToBeReviewed
                    .getFirst().getValue()));
            }
        }

        resultMap.put("reviewOrderUrgency", NO);
        resultMap.put("addCoverSheet", NO);
        resultMap.put("judgeNameAndTitle", approveDraftOrdersService.getJudgeTitleAndNameOfCurrentUser(caseData));

        return resultMap;
    }

    public Map<String, Object> initReviewFieldsForSelectedBundle(AdditionalApplicationsBundle bundle) {
        HashMap<String, Object> resultMap = new HashMap<>();

        C2DocumentBundle c2ToBeReviewed = getRelevantC2DocumentBundle(bundle);
        boolean isConfidential = YES.equals(bundle.getHasConfidentialC2())
            && !isEmpty(bundle.getC2DocumentBundleConfidential());
        if (!isEmpty(c2ToBeReviewed)) {
            resultMap.put("hasC2ToBeReview", YES);
            DocumentReference documentReference = (isEmpty(c2ToBeReviewed.getDraftOrdersBundle())) ? null :
                c2ToBeReviewed.getDraftOrdersBundle().getFirst().getValue().getDocument();

            resultMap.put("uploadedDraftOrder", documentReference);
            if (!isEmpty(c2ToBeReviewed.getDraftOrdersBundle())) {
                resultMap.put("reviewAdditionalAppDraftOrderId",
                    c2ToBeReviewed.getDraftOrdersBundle().getFirst().getId().toString());
            }
            resultMap.put("reviewAdditionalAppIsConfidential", isConfidential ? YES : NO);
            resultMap.put("c2AdditionalApplicationToBeReview", C2AdditionalApplicationEventData.builder()
                .routeType(c2ToBeReviewed.getRouteType())
                .applicantName(c2ToBeReviewed.getApplicantName())
                .type(c2ToBeReviewed.getType())
                .confidentialApplication(isConfidential
                    ? YES.getValue() + " - only HMCTS will be able to view this application"
                    : NO.getValue())
                .document(c2ToBeReviewed.getDocument())
                .applicationPermissionType(c2ToBeReviewed.getApplicationPermissionType())
                .applicationRelatesToAllChildren(c2ToBeReviewed.getApplicationRelatesToAllChildren())
                .childrenOnApplication(c2ToBeReviewed.getChildrenOnApplication())
                .applicationSummary(c2ToBeReviewed.getApplicationSummary())
                .hasSafeguardingRisk(c2ToBeReviewed.getHasSafeguardingRisk())
                .isHearingAdjournmentRequired(c2ToBeReviewed.getIsHearingAdjournmentRequired())
                .requestedHearingToAdjourn(c2ToBeReviewed.getRequestedHearingToAdjourn())
                .canBeConsideredAtNextHearing(c2ToBeReviewed.getCanBeConsideredAtNextHearing())
                .draftOrdersBundle(c2ToBeReviewed.getDraftOrdersBundle())
                .supplementsBundle(c2ToBeReviewed.getSupplementsBundle())
                .supportingEvidenceBundle(c2ToBeReviewed.getSupportingEvidenceBundle())
                .uploadedDateTime(c2ToBeReviewed.getUploadedDateTime())
                .build());
        } else {
            resultMap.put("hasC2ToBeReview", NO);
        }
        if (!isEmpty(bundle.getOtherApplicationsBundle())) {
            resultMap.put("hasOtherToBeReview", YES);
            resultMap.put("otherAdditionalApplicationToBeReview", bundle.getOtherApplicationsBundle());
        } else {
            resultMap.put("hasOtherToBeReview", NO);
        }
        return resultMap;
    }

    private C2DocumentBundle getRelevantC2DocumentBundle(AdditionalApplicationsBundle bundle) {
        if (YES.equals(bundle.getHasConfidentialC2()) && !isEmpty(bundle.getC2DocumentBundleConfidential())) {
            return bundle.getC2DocumentBundleConfidential();
        }
        return bundle.getC2DocumentBundle();
    }

    private List<Element<AdditionalApplicationsBundle>> getApplicationsToBeReviewed(CaseData caseData) {
        if (caseData.getAdditionalApplicationsBundle() != null) {
            return caseData.getAdditionalApplicationsBundle().stream()
                .filter(bundleElement -> !YES.equals(bundleElement.getValue().getApplicationReviewed()))
                .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public Element<AdditionalApplicationsBundle> getSelectedApplicationsToBeReviewed(CaseData caseData) {
        final List<Element<AdditionalApplicationsBundle>> applicationsBundlesToBeReviewed =
            getApplicationsToBeReviewed(caseData);

        if (applicationsBundlesToBeReviewed.size() == 1) {
            return applicationsBundlesToBeReviewed.getFirst();
        } else {
            ConfirmApplicationReviewedEventData eventData = caseData.getConfirmApplicationReviewedEventData();

            return findElement(eventData.getAdditionalApplicationToBeReviewedList().getValueCodeAsUUID(),
                applicationsBundlesToBeReviewed).orElseThrow();
        }
    }

    public List<Element<AdditionalApplicationsBundle>> markSelectedBundleAsReviewed(CaseData caseData) {
        List<Element<AdditionalApplicationsBundle>> additionalApplications = caseData.getAdditionalApplicationsBundle();

        Element<AdditionalApplicationsBundle> selectedApplication = getSelectedApplicationsToBeReviewed(caseData);

        return additionalApplications.stream().map(existingBundle -> {
                if (selectedApplication.getId().equals(existingBundle.getId())) {
                    return element(selectedApplication.getId(),
                        selectedApplication.getValue().toBuilder().applicationReviewed(YES).build());
                }
                return existingBundle;
            }
        ).collect(Collectors.toList());
    }

    public Map<String, Object> returnDraftOrderToApplicant(CaseData caseData,
                                                            Element<HearingOrdersBundle> hearingOrdersBundle,
                                                            UUID draftOrderId,
                                                            String requestedChanges) {
        Map<String, Object> updates = new HashMap<>();

        Element<HearingOrder> orderElement = hearingOrdersBundle.getValue().getAllOrdersAndConfidentialOrders().stream()
            .filter(order -> order.getId().equals(draftOrderId))
            .findFirst()
            .orElseThrow(() -> new HearingOrdersBundleNotFoundException(
                "No HearingOrder found with element id: " + draftOrderId
            ));

        Element<HearingOrder> rejectedOrder = hearingOrderGenerator.buildRejectedHearingOrder(
            orderElement,
            isBlank(requestedChanges) ? APPLICANT_CHANGES_REQUESTED : requestedChanges
        );

        if (orderElement.getValue().isConfidentialOrder()) {
            updates.putAll(addToConfidentialOrderBundle(hearingOrdersBundle, orderElement,
                caseData.getConfidentialRefusedOrders(), rejectedOrder));
        } else {
            List<Element<HearingOrder>> refusedOrders = defaultIfNull(caseData.getRefusedHearingOrders(),
                new ArrayList<>());
            refusedOrders.add(rejectedOrder);
            updates.put("refusedHearingOrders", refusedOrders);
        }

        hearingOrdersBundle.getValue().removeOrderElement(orderElement);
        updates.putAll(approveDraftOrdersService.updateHearingDraftOrdersBundle(caseData, hearingOrdersBundle));


        return updates;
    }

    public Map<String, Object> addRefusalOrders(CaseData caseData,
                                                Element<HearingOrdersBundle> selectedOrdersBundle,
                                                UUID draftOrderId) {
        ConfirmApplicationReviewedEventData eventData = caseData.getConfirmApplicationReviewedEventData();

        boolean isConfidential = YES.equals(eventData.getReviewAdditionalAppIsConfidential());

        // generate refusal order and add it to orderCollection
        Element<GeneratedOrder> refusalOrderDoc = refusalOrderService.buildRefusalOrder(caseData,
            eventData.getJudgeNameAndTitle(),
            eventData.getC2AdditionalApplicationToBeReview().getUploadedDateTime(),
            eventData.getReviewAdditionalAppRefusalReason(),
            isConfidential);

        List<Element<GeneratedOrder>> orderCollection = caseData.getOrderCollection();
        orderCollection.add(refusalOrderDoc);

        // update the draft order as rejected and move them to refused
        Element<HearingOrder> draftOrder = findElement(draftOrderId, selectedOrdersBundle.getValue()
            .getAllOrdersAndConfidentialOrders()).orElseThrow();

        Map<String, Object> updates = new HashMap<>();
        Element<HearingOrder> rejectedDraftOrder = approveDraftOrdersService.rejectDraftOrderWithRequestedChanges(
            caseData,
            updates,
            selectedOrdersBundle,
            draftOrder,
            eventData.getReviewAdditionalAppRefusalReason()
        );

        List<Element<HearingOrder>> rejectedOrders = getIfNull(caseData.getRefusedHearingOrders(), new ArrayList<>());
        rejectedOrders.add(rejectedDraftOrder);
        updates.put("refusedHearingOrders", rejectedOrders);

        selectedOrdersBundle.getValue().removeOrderElement(draftOrder);

        updates.put("orderCollection", orderCollection);

        return updates;
    }

    public List<ReviewCMOEvent> buildEventsToPublish(CaseData caseData, CaseData oldCaseData) {
        ConfirmApplicationReviewedEventData oldEventData = oldCaseData.getConfirmApplicationReviewedEventData();
        Element<AdditionalApplicationsBundle> selectedBundle = getSelectedApplicationsToBeReviewed(caseData);
        C2DocumentBundle rejectedC2Bundle = oldEventData.getC2AdditionalApplicationToBeReview();

        List<ReviewCMOEvent> eventsToPublish = new ArrayList<>();

        if (ApproveAdditionalAppOptions.REFUSE.equals(oldCaseData.getApproveAdditionalAppRouter())) {
            eventsToPublish.add(C2ApplicationRejectedEvent.builder()
                .caseData(caseData)
                .selectedAdditionalApplicationBundle(selectedBundle.getValue())
                .c2DocumentRefused(rejectedC2Bundle)
                .refusalOrderTitle(refusalOrderService.getRefusalOrderTitle(rejectedC2Bundle.getUploadedDateTime()))
                .build());
        } else {
            // TBC
            // eventsToPublish.addAll(draftOrdersEventNotificationBuilder.buildEventsToPublish(caseData));
        }

        return eventsToPublish;
    }
}
