package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.HearingOrdersBundleNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.C2AdditionalApplicationEventData;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;
import uk.gov.hmcts.reform.fpl.model.order.DraftOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.cmo.ApproveDraftOrdersService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewAdditionalApplicationService {
    public static final String ONLY_ONE_APPLICATION = "onlyOneApplicationToBeReviewed";
    private static final String FILE_NAME_PREFIX = "amended_%s";

    private final ApproveDraftOrdersService approveDraftOrdersService;

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

    public Map<String, Object> amendHearingOrdersBundlesDraft(CaseData caseData,
                                                        DocumentReference amendedOrderDocument) {
        AdditionalApplicationsBundle selectedAdditionalApplicationsBundle =
            getSelectedApplicationsToBeReviewed(caseData).getValue();

        boolean isC2Confidential = selectedAdditionalApplicationsBundle.getHasConfidentialC2().equals(YES);

        // Get the current existing draft order on the Application
        Element<DraftOrder> existingDraftOrder = isC2Confidential ?
            selectedAdditionalApplicationsBundle.getC2DocumentBundleConfidential().getDraftOrdersBundle().getFirst()
            : selectedAdditionalApplicationsBundle.getC2DocumentBundle().getDraftOrdersBundle().getFirst();

        Element<HearingOrdersBundle> selectedBundle = getBundleFromDraftOrder(caseData, existingDraftOrder.getId());

        if (isC2Confidential) {
            //logic to fetch and search for correct confidential collection to put the update bundle in

            return approveDraftOrdersService.updateHearingDraftOrdersBundle(caseData, selectedBundle);
        } else {
            // find and update the order in the bundle to match the one uploaded
            selectedBundle.getValue().getOrders().stream()
                .filter(order -> order.getId().equals(existingDraftOrder.getId()))
                .findFirst()
                .orElseThrow(() -> new HearingOrdersBundleNotFoundException(
                    "No HearingOrder found with element id: " + existingDraftOrder.getId()
                ))
                .getValue().toBuilder()
                    .amendedDate(LocalDate.now())
                    .order(amendedOrderDocument.toBuilder()
                        .filename(String.format(FILE_NAME_PREFIX, existingDraftOrder.getValue().getTitle()))
                        .build())
                    .lastUploadedOrder(existingDraftOrder.getValue().getDocument())
                .build();

            List<Element<HearingOrdersBundle>> amendedHearingOrdersBundlesDrafts = caseData.getHearingOrdersBundlesDrafts()
                .stream().filter(element -> element.getId() != selectedBundle.getId())
                .collect(Collectors.toList());

            amendedHearingOrdersBundlesDrafts.add(selectedBundle);

            return Map.of("hearingOrdersBundlesDrafts", amendedHearingOrdersBundlesDrafts);
        }
    }

    public Element<HearingOrdersBundle> getBundleFromDraftOrder(CaseData caseData,
                                                                UUID draftOrderId) {
        boolean isConfidential =
            getSelectedApplicationsToBeReviewed(caseData).getValue().getHasConfidentialC2().equals(YES);

        // NEEDS TO BE UPDATED TO INCLUDE LA/CHILD/RESP etc confidential collections!!!!
        return caseData.getHearingOrdersBundlesDrafts().stream()
            .filter(bundleElement -> {
                if (isConfidential) {
                    return bundleElement.getValue().getOrdersCTSC().stream()
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
    }

    public Map<String, Object> amendAdditionalApplicationsBundle(CaseData caseData,
                                                                 DocumentReference amendedOrderDocument) {
        AdditionalApplicationsBundle selectedAdditionalApplicationsBundle =
            getSelectedApplicationsToBeReviewed(caseData).getValue();

        // Get the current existing draft order on the Application
        Element<DraftOrder> existingDraftOrder = selectedAdditionalApplicationsBundle.getHasConfidentialC2().equals(YES) ?
            selectedAdditionalApplicationsBundle.getC2DocumentBundleConfidential().getDraftOrdersBundle().getFirst()
            : selectedAdditionalApplicationsBundle.getC2DocumentBundle().getDraftOrdersBundle().getFirst();

        // Create filtered list of existing additional apps without the selected one
        List<Element<AdditionalApplicationsBundle>> amendedAdditionalApplications = caseData.getAdditionalApplicationsBundle()
            .stream()
            .filter(bundleElement -> bundleElement.getId() != getSelectedApplicationsToBeReviewed(caseData).getId())
            .collect(Collectors.toList());

        // Add the amended order to the additional application bundle
        AdditionalApplicationsBundle amendedAdditionalApplicationsBundle =
            amendDraftOrderInAdditionalAppsBundle(amendedOrderDocument, selectedAdditionalApplicationsBundle, existingDraftOrder);

        // add the amended application back with the same Id
        amendedAdditionalApplications
            .add(element(getSelectedApplicationsToBeReviewed(caseData).getId(), amendedAdditionalApplicationsBundle));

        return Map.of("additionalApplicationsBundle",  amendedAdditionalApplications);
    }

    public AdditionalApplicationsBundle amendDraftOrderInAdditionalAppsBundle(DocumentReference amendedOrderDocument,
                                                                              AdditionalApplicationsBundle selectedAdditionalApplicationsBundle,
                                                                              Element<DraftOrder> existingDraftOrder) {
        DraftOrder amendedDraftOrder = existingDraftOrder.getValue().toBuilder()
            .document(amendedOrderDocument)
            .title(FILE_NAME_PREFIX + existingDraftOrder.getValue().getTitle())
            .build();

        return selectedAdditionalApplicationsBundle.getHasConfidentialC2().equals(YES) ?
            selectedAdditionalApplicationsBundle.toBuilder()
            .c2DocumentBundleConfidential(selectedAdditionalApplicationsBundle.getC2DocumentBundle().toBuilder()
                                          .draftOrdersBundle(List.of(element(existingDraftOrder.getId(), amendedDraftOrder)))
                                          .build()).build()
            : selectedAdditionalApplicationsBundle.toBuilder()
              .c2DocumentBundle(selectedAdditionalApplicationsBundle.getC2DocumentBundle().toBuilder()
                                .draftOrdersBundle(List.of(element(existingDraftOrder.getId(), amendedDraftOrder)))
                                .build()).build();
    }
}
