package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.orders.ApproveDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.ApproveOrderUrgencyOption;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(ApproveDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ApproveDraftOrdersControllerAboutToStartTest extends AbstractCallbackTest {

    private final String hearing1 = "Test hearing 21st August 2020";
    private final String hearing2 = "Test hearing 9th April 2021";

    private final Element<HearingOrder> agreedCMO = element(buildDraftOrder(hearing1, AGREED_CMO));
    private final Element<HearingOrder> draftCMO = element(buildDraftOrder(hearing1, DRAFT_CMO));
    private final Element<HearingOrder> draftOrder1 = element(buildDraftOrder(hearing1, C21));
    private final Element<HearingOrder> draftOrder2 = element(buildDraftOrder(hearing2, C21));

    ApproveDraftOrdersControllerAboutToStartTest() {
        super("approve-draft-orders");
    }

    @Test
    void shouldReturnCorrectDataWhenMultipleHearingDraftOrdersBundlesExist() {
        UUID hearingOrdersBundle1 = UUID.randomUUID();
        UUID hearingOrdersBundle2 = UUID.randomUUID();

        List<Element<HearingOrdersBundle>> hearingOrdersBundles = List.of(
            buildHearingDraftOrdersBundles(hearingOrdersBundle1, hearing1, newArrayList(agreedCMO, draftOrder1)),
            buildHearingDraftOrdersBundles(hearingOrdersBundle2, hearing2, newArrayList(draftCMO, draftOrder2)));

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(newArrayList(agreedCMO, draftCMO))
            .hearingOrdersBundlesDrafts(hearingOrdersBundles)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        DynamicList bundlesList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(hearingOrdersBundles.stream().map(bundle -> DynamicListElement.builder()
                .code(bundle.getId())
                .label(bundle.getValue().getHearingName())
                .build())
                .collect(Collectors.toList()))
            .build();

        CaseData responseData = extractCaseData(response);

        assertThat(responseData.getNumDraftCMOs()).isEqualTo("MULTI");
        assertThat(responseData.getCmoToReviewList()).isEqualTo(
            mapper.convertValue(bundlesList, new TypeReference<Map<String, Object>>() {
            }));
    }

    @Test
    void shouldReturnAgreedCMOWhenOneHearingBundleExistsWithADraftCMOsReadyForApproval() {
        UUID hearingOrdersBundleId = UUID.randomUUID();
        Element<HearingOrdersBundle> hearingOrdersBundle =
            buildHearingDraftOrdersBundles(hearingOrdersBundleId, hearing1, newArrayList(agreedCMO, draftCMO));

        CaseData caseData = CaseData.builder().draftUploadedCMOs(List.of(agreedCMO, draftCMO))
            .hearingOrdersBundlesDrafts(singletonList(hearingOrdersBundle))
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .reviewDraftOrdersData(ReviewDraftOrdersData.builder()
                .reviewDecision1(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
                .build())
            .build();

        ReviewDraftOrdersData expectedReviewDraftOrdersData = ReviewDraftOrdersData.builder()
            .cmoDraftOrderTitle(agreedCMO.getValue().getTitle())
            .cmoDraftOrderDocument(agreedCMO.getValue().getOrder())
            .draftCMOExists("Y")
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);
        CaseData responseData = extractCaseData(callbackResponse);

        assertThat(responseData.getReviewCMODecision())
            .isEqualTo(ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build());
        assertThat(responseData.getNumDraftCMOs()).isEqualTo("SINGLE");
        assertThat(responseData.getReviewDraftOrdersData()).isEqualTo(expectedReviewDraftOrdersData);
    }

    @Test
    void shouldReturnCorrectDataWhenNoDraftCMOsReadyForApproval() {
        CaseData caseData = CaseData.builder().draftUploadedCMOs(emptyList())
            .hearingOrdersBundlesDrafts(emptyList()).build();

        CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));
        assertThat(updatedCaseData.getNumDraftCMOs()).isEqualTo("NONE");
    }

    @Test
    void shouldReturnCorrectDataWhenNoCMOsExistForReadyForApprovalInTheSelectedBundle() {
        UUID hearingOrdersBundleId = UUID.randomUUID();
        Element<HearingOrdersBundle> hearingOrdersBundle =
            buildHearingDraftOrdersBundles(hearingOrdersBundleId, hearing1, newArrayList(draftCMO));

        CaseData caseData = CaseData.builder().draftUploadedCMOs(newArrayList(draftCMO))
            .hearingOrdersBundlesDrafts(singletonList(hearingOrdersBundle)).build();

        CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));
        assertThat(updatedCaseData.getNumDraftCMOs()).isEqualTo("NONE");
    }

    @Test
    void shouldClearOrderReviewUrgencyField() {
        CaseData caseData = CaseData.builder()
            .orderReviewUrgency(ApproveOrderUrgencyOption.builder()
                .urgency(List.of(YesNo.YES))
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseData);
        CaseData responseData = extractCaseData(callbackResponse);

        assertThat(Optional.ofNullable(Optional.ofNullable(responseData.getOrderReviewUrgency())
            .orElse(ApproveOrderUrgencyOption.builder().build()).getUrgency()).orElse(List.of()))
            .asList().isEmpty();
    }

    private Element<HearingOrdersBundle> buildHearingDraftOrdersBundles(
        UUID hearingOrdersBundleId, String hearing, List<Element<HearingOrder>> orders) {
        return element(hearingOrdersBundleId,
            HearingOrdersBundle.builder().hearingId(UUID.randomUUID())
                .orders(orders)
                .hearingName(hearing).build());
    }

    private HearingOrder buildDraftOrder(String hearing, HearingOrderType orderType) {
        return HearingOrder.builder()
            .hearing(hearing)
            .title(hearing)
            .order(testDocumentReference())
            .type(orderType)
            .status(DRAFT_CMO.equals(orderType) ? DRAFT : SEND_TO_JUDGE).build();
    }
}
