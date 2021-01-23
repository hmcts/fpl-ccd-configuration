package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.exceptions.HearingOrdersBundleNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.CMO;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewCMOService {

    private final ObjectMapper mapper;
    private final Time time;

    /**
     * That methods shouldn't be invoked without any cmo selected as the outcome is unexpected.
     * There is dedicated method below to support this functionality.
     */
    public DynamicList buildDynamicList(CaseData caseData) {
        List<Element<HearingOrdersBundle>> cmosReadyForApproval = caseData.getHearingOrdersBundlesDrafts();
        Element<HearingOrdersBundle> selectedCMO = getSelectedHearingDraftOrdersBundle(caseData);

        return asDynamicList(cmosReadyForApproval, selectedCMO.getId(), HearingOrdersBundle::getHearingName);
    }

    public DynamicList buildUnselectedDynamicList(CaseData caseData) {
        List<Element<HearingOrdersBundle>> orderBundlesForApproval = caseData.getHearingOrdersBundlesDrafts();

        return asDynamicList(orderBundlesForApproval, null, HearingOrdersBundle::getHearingName);
    }

    public Map<String, Object> getPageDisplayControls(CaseData caseData) {
        List<Element<HearingOrdersBundle>> draftOrdersReadyForApproval = caseData.getHearingOrdersBundlesDrafts();
        Map<String, Object> data = new HashMap<>();
        String numDraftCMOs = "numDraftCMOs";

        switch (draftOrdersReadyForApproval.size()) {
            case 0:
                data.put(numDraftCMOs, "NONE");
                break;
            case 1:
                HearingOrdersBundle hearingOrdersBundle = draftOrdersReadyForApproval.get(0).getValue();
                data.put(numDraftCMOs, "SINGLE");
                data.putAll(buildDraftOrdersReviewData(hearingOrdersBundle));
                break;
            default:
                data.put(numDraftCMOs, "MULTI");
                data.put("cmoToReviewList", buildUnselectedDynamicList(caseData));
                break;
        }

        return data;
    }

    public Map<String, Object> populateDraftOrdersData(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        Element<HearingOrdersBundle> selectedCMO = getSelectedHearingDraftOrdersBundle(caseData);

        data.put("reviewDraftOrdersTitles", buildDraftOrdersBundleSummary(caseData.getCaseName(), selectedCMO.getValue()));
        data.putAll(buildDraftOrdersReviewData(selectedCMO.getValue()));

        return data;
    }

    public Element<HearingOrder> getCMOToSeal(
        Element<ReviewDecision> reviewDecisionElement,
        Element<HearingOrder> hearingOrderElement
    ) {
        ReviewDecision reviewDecision = reviewDecisionElement.getValue();
        DocumentReference order;

        if (JUDGE_AMENDS_DRAFT.equals(reviewDecision.getDecision())) {
            order = reviewDecision.getJudgeAmendedDocument();
        } else {
            order = hearingOrderElement.getValue().getOrder();
        }
        return element(hearingOrderElement.getId(), hearingOrderElement.getValue().toBuilder()
            .dateIssued(time.now().toLocalDate())
            .status(CMOStatus.APPROVED)
            .order(order)
            .build());
    }

    public List<Element<HearingOrder>> getCMOsReadyForApproval(CaseData caseData) {
        return caseData.getDraftUploadedCMOs().stream()
            .filter(cmo -> cmo.getValue().getStatus().equals(SEND_TO_JUDGE))
            .collect(Collectors.toList());
    }

    public Element<HearingOrdersBundle> getSelectedHearingDraftOrdersBundle(CaseData caseData) {
        List<Element<HearingOrdersBundle>> ordersBundleReadyForApproval = caseData.getHearingOrdersBundlesDrafts();
        if (ordersBundleReadyForApproval.size() > 1) {
            UUID selectedCMOCode = getSelectedCMOId(caseData.getCmoToReviewList());

            return ordersBundleReadyForApproval.stream()
                .filter(element -> element.getId().equals(selectedCMOCode))
                .findFirst()
                .orElseThrow(() -> new HearingOrdersBundleNotFoundException(
                    "Could not find draft cmo with id " + selectedCMOCode));
        } else {
            return ordersBundleReadyForApproval.get(0);
        }
    }

    public Element<HearingOrdersBundle> getSelectedCMO(CaseData caseData) {
        List<Element<HearingOrdersBundle>> readyForApproval = caseData.getHearingOrdersBundlesDrafts();
        if (readyForApproval.size() > 1) {
            UUID selectedCMOCode = getSelectedCMOId(caseData.getCmoToReviewList());

            return readyForApproval.stream()
                .filter(element -> element.getId().equals(selectedCMOCode))
                .findFirst()
                .orElseThrow(() -> new CMONotFoundException("Could not find draft cmo with id " + selectedCMOCode));
        } else {
            return readyForApproval.get(0);
        }
    }

    public HearingOrder getLatestSealedCMO(CaseData caseData) {
        List<Element<HearingOrder>> sealedCMOs = caseData.getSealedCMOs();
        if (!sealedCMOs.isEmpty()) {
            return sealedCMOs.get(sealedCMOs.size() - 1).getValue();
        } else {
            throw new CMONotFoundException("No sealed CMOS found");
        }
    }

    public State getStateBasedOnNextHearing(CaseData caseData, ReviewDecision reviewDecision, UUID cmoID) {
        State currentState = caseData.getState();
        Optional<HearingBooking> nextHearingBooking = caseData.getNextHearingAfterCmo(cmoID);

        if (nextHearingBooking.isPresent()
            && reviewDecision.hasReviewOutcomeOf(SEND_TO_ALL_PARTIES)
            && nextHearingBooking.get().isOfType(HearingType.FINAL)) {
            return State.FINAL_HEARING;
        }
        return currentState;
    }

    public List<Element<HearingOrdersBundle>> updateHearingDraftOrdersBundle(CaseData caseData, Element<HearingOrdersBundle> selectedOrdersBundle, List<Element<HearingOrder>> ordersInBundle) {
        List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = caseData.getHearingOrdersBundlesDrafts();
        if (ordersInBundle.isEmpty()) {
            hearingOrdersBundlesDrafts.removeIf(bundle -> bundle.getId().equals(selectedOrdersBundle.getId()));
        } else {
            hearingOrdersBundlesDrafts.stream()
                .filter(bundle -> bundle.getId().equals(selectedOrdersBundle.getId()))
                .forEach(bundle -> bundle.getValue().toBuilder().orders(ordersInBundle).build());
        }
        return hearingOrdersBundlesDrafts;
    }

    private UUID getSelectedCMOId(Object dynamicList) {
        //see RDM-5696 and RDM-6651
        if (dynamicList instanceof String) {
            return UUID.fromString(dynamicList.toString());
        }
        return mapper.convertValue(dynamicList, DynamicList.class).getValueCode();
    }

    private String buildDraftOrdersBundleSummary(String caseName, HearingOrdersBundle hearingOrdersBundle) {
        String ordersSummary = unwrapElements(hearingOrdersBundle.getOrders()).stream()
            .map(order -> {
                if (order.getType().isCmo()) {
                    return String.format("%s for %s", CMO, order.getHearing());
                } else {
                    return String.format("%s Order - %s for %s", C21, order.getTitle(), hearingOrdersBundle.getHearingName());
                }
            }).collect(Collectors.joining("\n"));
        return String.format("%s has sent the following orders for approval.\n%s", caseName, ordersSummary);
    }

    private Map<String, Object> buildDraftOrdersReviewData(HearingOrdersBundle ordersBundle) {
        Map<String, Object> data = new HashMap<>();

        int counter = 1;
        for (Element<HearingOrder> orderElement : ordersBundle.getOrders()) {
            if (orderElement.getValue().getType().isCmo()) {
                data.put("reviewCMODecision",
                    ReviewDecision.builder().hearing(orderElement.getValue().getTitle())
                        .document(orderElement.getValue().getOrder()).build());
            } else {
                data.put("reviewDecision_" + counter,
                    ReviewDecision.builder().hearing(orderElement.getValue().getTitle())
                        .document(orderElement.getValue().getOrder()).build());
                counter++;
            }
        }
        return data;
    }
}
