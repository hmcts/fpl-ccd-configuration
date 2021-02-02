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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApproveDraftOrdersService {

    private final ObjectMapper mapper;
    private final Time time;
    private final DraftOrderService draftOrderService;
    private final DocumentSealingService documentSealingService;

    private static final String ORDERS_TO_BE_SENT = "ordersToBeSent";

    /**
     * That methods shouldn't be invoked without any cmo selected as the outcome is unexpected.
     * There is dedicated method below to support this functionality.
     */
    public DynamicList buildDynamicList(CaseData caseData) {
        List<Element<HearingOrdersBundle>> bundlesReadyForApproval = getBundlesForApproval(caseData);
        Element<HearingOrdersBundle> selectedBundle = getSelectedHearingDraftOrdersBundle(caseData);

        return asDynamicList(bundlesReadyForApproval, selectedBundle.getId(), HearingOrdersBundle::getHearingName);
    }

    public DynamicList buildUnselectedDynamicList(CaseData caseData) {
        List<Element<HearingOrdersBundle>> orderBundlesForApproval = getBundlesForApproval(caseData);
        return asDynamicList(orderBundlesForApproval, null, HearingOrdersBundle::getHearingName);
    }

    public Map<String, Object> getPageDisplayControls(CaseData caseData) {
        List<Element<HearingOrdersBundle>> draftOrdersReadyForApproval = getBundlesForApproval(caseData);
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
                DynamicList value = buildUnselectedDynamicList(caseData);
                data.put("cmoToReviewList", value);
                break;
        }

        return data;
    }

    public Map<String, Object> populateDraftOrdersData(CaseData caseData) {
        Element<HearingOrdersBundle> selectedHearingOrdersBundle = getSelectedHearingDraftOrdersBundle(caseData);

        return buildDraftOrdersReviewData(selectedHearingOrdersBundle.getValue());
    }

    @SuppressWarnings("unchecked")
    public List<String> validateDraftOrdersReviewDecision(CaseData caseData, Map<String, Object> data) {
        Element<HearingOrdersBundle> selectedOrdersBundle = getSelectedHearingDraftOrdersBundle(caseData);

        List<HearingOrder> hearingOrders = unwrapElements(selectedOrdersBundle.getValue().getOrders());
        List<String> errors = new ArrayList<>();

        boolean noReviewDecisionExists = true;
        int counter = 1;
        for (HearingOrder order : hearingOrders) {
            if (order.getType().isCmo() && caseData.getReviewCMODecision() != null
                && caseData.getReviewCMODecision().getDecision() != null) {

                noReviewDecisionExists = false;
                validateReviewDecision(errors, caseData.getReviewCMODecision(), "CMO");
            } else {
                Map<String, Object> reviewDecisionMap = (Map<String, Object>) data.get("reviewDecision" + counter);
                ReviewDecision reviewDecision = mapper.convertValue(reviewDecisionMap, ReviewDecision.class);
                if (reviewDecision != null && reviewDecision.getDecision() != null) {

                    noReviewDecisionExists = false;
                    validateReviewDecision(errors, reviewDecision, "draft order " + counter);
                    counter++;
                }
            }
        }

        if (!hearingOrders.isEmpty() && noReviewDecisionExists) {
            errors.add("Approve, amend or reject draft orders");
        }

        return errors;
    }

    public Map<String, Object> reviewCMO(CaseData caseData, Element<HearingOrdersBundle> selectedOrdersBundle) {
        Map<String, Object> data = new HashMap<>();

        Element<HearingOrder> cmo = selectedOrdersBundle.getValue().getOrders(SEND_TO_JUDGE).stream()
            .filter(order -> order.getValue().getType().isCmo())
            .findFirst().orElse(null);

        if (cmo != null) {
            ReviewDecision cmoReviewDecision = caseData.getReviewCMODecision();
            if (cmoReviewDecision != null && cmoReviewDecision.getDecision() != null) {

                Element<HearingOrder> reviewedOrder;

                if (!JUDGE_REQUESTED_CHANGES.equals(cmoReviewDecision.getDecision())) {
                    reviewedOrder = buildSealedHearingOrder(cmoReviewDecision, cmo);

                    List<Element<HearingOrder>> sealedCMOs = caseData.getSealedCMOs();
                    sealedCMOs.add(reviewedOrder);
                    data.put("sealedCMOs", sealedCMOs);
                    data.put("state", getStateBasedOnNextHearing(caseData, cmoReviewDecision, reviewedOrder.getId()));
                } else {
                    reviewedOrder = buildRejectedHearingOrder(cmo, cmoReviewDecision.getChangesRequestedByJudge());
                }

                caseData.getDraftUploadedCMOs().remove(cmo);
                updateHearingCMO(caseData, cmo.getId());

                data.put("hearingDetails", caseData.getHearingDetails());
                data.put("draftUploadedCMOs", caseData.getDraftUploadedCMOs());
                data.put("hearingOrdersBundlesDrafts", draftOrderService.migrateCmoDraftToOrdersBundles(caseData));

                data.put(ORDERS_TO_BE_SENT, newArrayList(reviewedOrder));
            }
        }
        return data;
    }

    public List<Element<HearingOrder>> getCMOsReadyForApproval(CaseData caseData) {
        return caseData.getDraftUploadedCMOs().stream()
            .filter(cmo -> cmo.getValue().getStatus().equals(SEND_TO_JUDGE))
            .collect(toList());
    }

    public Element<HearingOrdersBundle> getSelectedHearingDraftOrdersBundle(CaseData caseData) {
        List<Element<HearingOrdersBundle>> ordersBundleReadyForApproval = getBundlesForApproval(caseData);
        if (ordersBundleReadyForApproval.size() > 1) {
            UUID selectedHearingDraftOrdersBundleCode = getSelectedCMOId(caseData.getCmoToReviewList());

            return ordersBundleReadyForApproval.stream()
                .filter(element -> element.getId().equals(selectedHearingDraftOrdersBundleCode))
                .findFirst()
                .orElseThrow(() -> new HearingOrdersBundleNotFoundException(
                    "Could not find hearing draft orders bundle with id " + selectedHearingDraftOrdersBundleCode));
        } else {
            return ordersBundleReadyForApproval.get(0);
        }
    }

    public HearingOrder getLatestSealedCMO(CaseData caseData) {
        List<Element<HearingOrder>> sealedCMOs = caseData.getSealedCMOs();
        if (!sealedCMOs.isEmpty()) {
            return sealedCMOs.get(sealedCMOs.size() - 1).getValue();
        } else {
            throw new CMONotFoundException("No sealed CMOs found");
        }
    }

    @SuppressWarnings("unchecked")
    public void reviewC21Orders(CaseData caseData, Map<String, Object> data,
                                Element<HearingOrdersBundle> selectedOrdersBundle) {
        List<Element<HearingOrder>> draftOrders = selectedOrdersBundle.getValue().getOrders().stream()
            .filter(order -> !order.getValue().getType().isCmo()).collect(toList());

        List<Element<HearingOrder>> ordersToBeSent = defaultIfNull((
            List<Element<HearingOrder>>) data.get(ORDERS_TO_BE_SENT), newArrayList());

        int counter = 1;
        List<Element<GeneratedOrder>> reviewedOrders = caseData.getOrderCollection();

        for (Element<HearingOrder> orderElement : draftOrders) {
            Map<String, Object> reviewDecisionMap = (Map<String, Object>) data.get("reviewDecision" + counter);
            ReviewDecision reviewDecision = mapper.convertValue(reviewDecisionMap, ReviewDecision.class);

            if (reviewDecision != null && reviewDecision.getDecision() != null) {
                Element<HearingOrder> reviewedOrder;

                if (!JUDGE_REQUESTED_CHANGES.equals(reviewDecision.getDecision())) {
                    reviewedOrder = buildSealedHearingOrder(reviewDecision, orderElement);
                    reviewedOrders.add(buildBlankOrder(caseData, selectedOrdersBundle, reviewedOrder));

                    ordersToBeSent.add(reviewedOrder);
                } else {
                    ordersToBeSent.add(
                        buildRejectedHearingOrder(orderElement, reviewDecision.getChangesRequestedByJudge()));
                }
                selectedOrdersBundle.getValue().getOrders().remove(orderElement);
            }
            counter++;
        }

        if (ordersToBeSent.isEmpty()) {
            data.remove(ORDERS_TO_BE_SENT);
        } else {
            data.put(ORDERS_TO_BE_SENT, ordersToBeSent);
        }

        updateHearingDraftOrdersBundle(caseData, selectedOrdersBundle);
        data.put("orderCollection", reviewedOrders);
        data.put("hearingOrdersBundlesDrafts", caseData.getHearingOrdersBundlesDrafts());
    }

    private void updateHearingDraftOrdersBundle(CaseData caseData, Element<HearingOrdersBundle> selectedOrdersBundle) {
        if (selectedOrdersBundle.getValue().getOrders().isEmpty()) {
            caseData.getHearingOrdersBundlesDrafts()
                .removeIf(bundle -> bundle.getId().equals(selectedOrdersBundle.getId()));
        } else {
            caseData.getHearingOrdersBundlesDrafts().stream()
                .filter(bundle -> bundle.getId().equals(selectedOrdersBundle.getId()))
                .forEach(bundle -> bundle.getValue().setOrders(selectedOrdersBundle.getValue().getOrders()));
        }
    }

    private Element<HearingOrder> buildSealedHearingOrder(
        ReviewDecision reviewDecision, Element<HearingOrder> hearingOrderElement) {
        DocumentReference order;

        if (JUDGE_AMENDS_DRAFT.equals(reviewDecision.getDecision())) {
            order = reviewDecision.getJudgeAmendedDocument();
        } else {
            order = hearingOrderElement.getValue().getOrder();
        }

        return element(hearingOrderElement.getId(), hearingOrderElement.getValue().toBuilder()
            .dateIssued(time.now().toLocalDate())
            .status(CMOStatus.APPROVED)
            .order(documentSealingService.sealDocument(order))
            .lastUploadedOrder(order)
            .build());
    }

    private Element<HearingOrder> buildRejectedHearingOrder(Element<HearingOrder> cmo, String changesRequested) {
        return element(cmo.getId(), cmo.getValue().toBuilder()
            .status(CMOStatus.RETURNED)
            .requestedChanges(changesRequested)
            .build());
    }

    private void updateHearingCMO(CaseData caseData, UUID cmoId) {
        defaultIfNull(caseData.getHearingDetails(), new ArrayList<Element<HearingBooking>>()).stream()
            .filter(hearing -> cmoId.equals(hearing.getValue().getCaseManagementOrderId()))
            .findFirst()
            .ifPresent(h -> h.getValue().setCaseManagementOrderId(null));
    }

    private Element<GeneratedOrder> buildBlankOrder(
        CaseData caseData, Element<HearingOrdersBundle> selectedOrdersBundle, Element<HearingOrder> sealedOrder) {

        Element<HearingBooking> hearingElement =
            defaultIfNull(caseData.getHearingDetails(), new ArrayList<Element<HearingBooking>>())
                .stream()
                .filter(hearing -> Objects.equals(hearing.getId(), selectedOrdersBundle.getValue().getHearingId()))
                .findFirst().orElse(null);

        HearingOrder order = sealedOrder.getValue();

        return element(sealedOrder.getId(), GeneratedOrder.builder()
            .type(BLANK_ORDER.getLabel())
            .title(order.getTitle())
            .document(order.getOrder())
            .dateOfIssue(order.getDateIssued() != null ? formatLocalDateToString(order.getDateIssued(), DATE) : null)
            .judgeAndLegalAdvisor(hearingElement != null
                ? getSelectedJudge(hearingElement.getValue().getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge())
                : null)
            .date(formatLocalDateTimeBaseUsingFormat(time.now(), TIME_DATE))
            .children(caseData.getAllChildren())
            .build());
    }

    private State getStateBasedOnNextHearing(CaseData caseData, ReviewDecision reviewDecision, UUID cmoID) {
        State currentState = caseData.getState();
        Optional<HearingBooking> nextHearingBooking = caseData.getNextHearingAfterCmo(cmoID);

        if (nextHearingBooking.isPresent()
            && reviewDecision.hasReviewOutcomeOf(SEND_TO_ALL_PARTIES)
            && nextHearingBooking.get().isOfType(HearingType.FINAL)) {
            return State.FINAL_HEARING;
        }
        return currentState;
    }

    private List<Element<HearingOrdersBundle>> getBundlesForApproval(CaseData caseData) {
        return caseData.getHearingOrdersBundlesDrafts().stream()
            .filter(bundle -> isNotEmpty(bundle.getValue().getOrders(SEND_TO_JUDGE)))
            .collect(toList());
    }

    private UUID getSelectedCMOId(Object dynamicList) {
        //see RDM-5696 and RDM-6651
        if (dynamicList instanceof String) {
            return UUID.fromString(dynamicList.toString());
        }
        return mapper.convertValue(dynamicList, DynamicList.class).getValueCode();
    }

    private Map<String, Object> buildDraftOrdersReviewData(HearingOrdersBundle ordersBundle) {
        Map<String, Object> data = new HashMap<>();

        int counter = 1;

        data.put("draftCMOExists", "N");
        for (Element<HearingOrder> orderElement : ordersBundle.getOrders(SEND_TO_JUDGE)) {

            if (orderElement.getValue().getType().isCmo()) {
                data.put("cmoDraftOrderTitle", orderElement.getValue().getTitle());
                data.put("cmoDraftOrderDocument", orderElement.getValue().getOrder());
                data.put("draftCMOExists", "Y");
            } else {
                data.put(String.format("draftOrder%dTitle", counter), orderElement.getValue().getTitle());
                data.put(String.format("draftOrder%dDocument", counter), orderElement.getValue().getOrder());
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

    private void validateReviewDecision(
        List<String> errors, ReviewDecision reviewDecision, String orderName) {
        if (JUDGE_AMENDS_DRAFT.equals(reviewDecision.getDecision())
            && reviewDecision.getJudgeAmendedDocument() == null) {
            errors.add(String.format("Add the new %s", orderName));
        } else if (JUDGE_REQUESTED_CHANGES.equals(reviewDecision.getDecision())
            && isBlank(reviewDecision.getChangesRequestedByJudge())) {
            errors.add(String.format("Add what the LA needs to change on the %s", orderName));
        }
    }
}
