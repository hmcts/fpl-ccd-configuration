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
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.CMO;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewCMOService {

    private final ObjectMapper mapper;
    private final Time time;
    private final DraftOrderService draftOrderService;
    private final DocumentSealingService documentSealingService;

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

    public Map<String, Object> reviewCMO(CaseData caseData, Element<HearingOrdersBundle> selectedOrdersBundle) {
        Map<String, Object> data = new HashMap<>();
        Element<HearingOrder> cmo = selectedOrdersBundle.getValue().getOrders().stream()
            .filter(order -> order.getValue().getType().isCmo())
            .findFirst().orElse(null);

        if (cmo != null) {
            ReviewDecision cmoReviewDecision = caseData.getReviewCMODecision();
            if (cmoReviewDecision.getDecision() != null) {
                if (!JUDGE_REQUESTED_CHANGES.equals(cmoReviewDecision.getDecision())) {
                    Element<HearingOrder> cmoToSeal = getCMOToSeal(cmoReviewDecision, cmo);
                    cmoToSeal.getValue().setLastUploadedOrder(cmoToSeal.getValue().getOrder());
                    cmoToSeal.getValue().setOrder(documentSealingService.sealDocument(cmoToSeal.getValue().getOrder()));

                    List<Element<HearingOrder>> sealedCMOs = caseData.getSealedCMOs();
                    sealedCMOs.add(cmoToSeal);

                    data.put("sealedCMOs", sealedCMOs);
                    data.put("state", getStateBasedOnNextHearing(caseData, cmoReviewDecision, cmoToSeal.getId()));
                }
                //TODO: check if draft order need to be removed when judge requests changes for CMO?
                caseData.getDraftUploadedCMOs().remove(cmo);

                data.put("draftUploadedCMOs", caseData.getDraftUploadedCMOs());
                data.put("hearingOrdersBundlesDrafts", draftOrderService.migrateCmoDraftToOrdersBundles(caseData));
            }
        }

        return data;
    }

    public Element<HearingOrder> getCMOToSeal(ReviewDecision reviewDecision, Element<HearingOrder> cmo) {
        DocumentReference order;

        if (JUDGE_AMENDS_DRAFT.equals(reviewDecision.getDecision())) {
            order = reviewDecision.getJudgeAmendedDocument();
        } else {
            order = cmo.getValue().getOrder();
        }
        return element(cmo.getId(), cmo.getValue().toBuilder()
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

    @SuppressWarnings("unchecked")
    public void reviewC21Orders(CaseData caseData, Map<String, Object> data,
                                Element<HearingOrdersBundle> selectedOrdersBundle) {
        List<Element<HearingOrder>> draftOrders = selectedOrdersBundle.getValue().getOrders().stream()
            .filter(order -> !order.getValue().getType().isCmo()).collect(Collectors.toList());

        int counter = 1;
        List<Element<GeneratedOrder>> reviewedOrders = caseData.getOrderCollection();
        for (Element<HearingOrder> orderElement : draftOrders) {
            Map<String, Object> reviewDecisionMap = (Map<String, Object>) data.get("reviewDecision" + counter);
            ReviewDecision reviewDecision = mapper.convertValue(reviewDecisionMap, ReviewDecision.class);
            if (reviewDecision != null && reviewDecision.getDecision() != null) {
                if (!JUDGE_REQUESTED_CHANGES.equals(reviewDecision.getDecision())) {
                    GeneratedOrder sealedC21Order = getSealedC21Order(orderElement, reviewDecision);
                    reviewedOrders.add(element(orderElement.getId(), sealedC21Order));
                }
                selectedOrdersBundle.getValue().getOrders().remove(orderElement);
            }
            counter++;
        }
        if (selectedOrdersBundle.getValue().getOrders().isEmpty()) {
            caseData.getHearingOrdersBundlesDrafts().removeIf(bundle -> bundle.getId().equals(selectedOrdersBundle.getId()));
        } else {
            caseData.getHearingOrdersBundlesDrafts().stream()
                .filter(bundle -> bundle.getId().equals(selectedOrdersBundle.getId()))
                .forEach(bundle -> bundle.getValue().setOrders(selectedOrdersBundle.getValue().getOrders()));
        }
        data.put("orderCollection", reviewedOrders);
        data.put("hearingOrdersBundlesDrafts", caseData.getHearingOrdersBundlesDrafts());

    }

    public GeneratedOrder getSealedC21Order(Element<HearingOrder> orderElement, ReviewDecision reviewDecision) {
        HearingOrder draftOrder = orderElement.getValue();
        DocumentReference order;

        if (JUDGE_AMENDS_DRAFT.equals(reviewDecision.getDecision())) {
            order = reviewDecision.getJudgeAmendedDocument();
        } else {
            order = orderElement.getValue().getOrder();
        }

        return GeneratedOrder.builder()
            .type(BLANK_ORDER.getLabel())
            .title(draftOrder.getTitle())
            .document(documentSealingService.sealDocument(order))
            .dateOfIssue(draftOrder.getDateIssued() != null
                ? formatLocalDateToString(draftOrder.getDateIssued(), DATE) : null)
            .judgeAndLegalAdvisor(null) // TODO: set judge and legal advisor
            .date(formatLocalDateTimeBaseUsingFormat(time.now(), TIME_DATE))
            //.children(getChildren(BLANK_ORDER, caseData)) //TODO
            .build();
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
                data.put("draftCMOExists", "Y");
            } else {
                data.put("reviewDecision" + counter,
                    ReviewDecision.builder().hearing(orderElement.getValue().getTitle())
                        .document(orderElement.getValue().getOrder()).build());
                counter++;
            }
        }

        if (counter > 1) {
            String numOfDraftOrders = IntStream.range(1, counter)
                .mapToObj(String::valueOf).collect(Collectors.joining(""));
            data.put("draftBlankOrdersCount", numOfDraftOrders);
        }
        return data;
    }
}
