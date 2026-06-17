package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.C2AdditionalApplicationEventData;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.service.cmo.ApproveDraftOrdersService;
import uk.gov.hmcts.reform.fpl.exceptions.HearingOrdersBundleNotFoundException;
import uk.gov.hmcts.reform.fpl.model.ConfidentialOrderBundle;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.cmo.HearingOrderGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
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
            return applicationsBundlesToBeReviewed.get(0);
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
                                                            UUID draftOrderId) {
        Map<String, Object> updates = new HashMap<>();

        Element<HearingOrder> orderElement = hearingOrdersBundle.getValue().getAllOrdersAndConfidentialOrders().stream()
            .filter(order -> order.getId().equals(draftOrderId))
            .findFirst()
            .orElseThrow(() -> new HearingOrdersBundleNotFoundException(
                "No HearingOrder found with element id: " + draftOrderId
            ));

        Element<HearingOrder> rejectedOrder = hearingOrderGenerator.buildRejectedHearingOrder(orderElement,
            APPLICANT_CHANGES_REQUESTED);

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

    private Map<String, List<Element<HearingOrder>>> addToConfidentialOrderBundle(
        Element<HearingOrdersBundle> draftBundle,
        Element<HearingOrder> draftOrderElement,
        ConfidentialOrderBundle<HearingOrder> targetBundle,
        Element<HearingOrder> orderToBeAdded
    ) {
        Map<String, List<Element<HearingOrder>>> updates = new HashMap<>();

        draftBundle.getValue().processAllConfidentialOrders((suffix, selectedDraftOrders) -> {
            if (isNotEmpty(selectedDraftOrders)
                && findElement(draftOrderElement.getId(), selectedDraftOrders).isPresent()) {
                List<Element<HearingOrder>> confidentialOrders =
                    defaultIfNull(targetBundle.getConfidentialOrdersBySuffix(suffix), new ArrayList<>());
                confidentialOrders.add(orderToBeAdded);
                updates.put(targetBundle.getFieldBaseName() + suffix, confidentialOrders);
                targetBundle.setConfidentialOrdersBySuffix(suffix, confidentialOrders);
            }
        });

        return updates;
    }
}
