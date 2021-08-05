package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.orders.ApproveDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_REQUESTED_CHANGES;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(ApproveDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ApproveDraftOrdersControllerValidateReviewDecisionMidEventTest extends AbstractCallbackTest {

    private final String validateDecisionEventPath = "validate-review-decision";
    private final String hearing = "Test hearing 21st August 2020";
    private final DocumentReference order = testDocumentReference();

    private final Element<HearingOrder> agreedCMO = element(buildDraftOrder(AGREED_CMO));
    private final Element<HearingOrder> draftOrder1 = element(buildDraftOrder(C21));
    private final Element<HearingOrder> draftOrder2 = element(buildDraftOrder(C21));

    @MockBean
    private FeatureToggleService featureToggleService;

    ApproveDraftOrdersControllerValidateReviewDecisionMidEventTest() {
        super("approve-draft-orders");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnErrorsWhenReviewDecisionIsJudgeAmendOrderAndNewOrderIsMissing(boolean servingOthersToggledOn) {
        UUID hearingOrdersBundleId = UUID.randomUUID();

        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            hearingOrdersBundleId, newArrayList(agreedCMO, draftOrder1));

        ReviewDraftOrdersData reviewDraftOrdersData = ReviewDraftOrdersData.builder()
            .reviewDecision1(ReviewDecision.builder().decision(JUDGE_AMENDS_DRAFT).build())
            .build();

        CaseData caseData = CaseData.builder()
            .others(Others.builder().firstOther(Other.builder().name("test1").build()).build())
            .draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .cmoToReviewList(hearingOrdersBundleId.toString())
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .reviewDraftOrdersData(reviewDraftOrdersData).build();

        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(servingOthersToggledOn);
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, validateDecisionEventPath);

        assertThat(callbackResponse.getErrors()).containsOnly("Add the new draft order 1");
        assertThat(callbackResponse.getData()).doesNotContainKeys("hasOthers", "others_label", "reviewCMOShowOthers")
            .containsEntry("othersSelector", null);
    }

    @Test
    void shouldReturnErrorsWhenReviewDecisionIsJudgeRequestsChangesAndRequestedChangesTextIsMissing() {
        UUID hearingOrdersBundleId = UUID.randomUUID();

        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            hearingOrdersBundleId, newArrayList(draftOrder1, draftOrder2));

        ReviewDraftOrdersData reviewDraftOrdersData = ReviewDraftOrdersData.builder()
            .reviewDecision1(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .reviewDecision2(ReviewDecision.builder().decision(JUDGE_REQUESTED_CHANGES).build())
            .build();

        CaseData caseData = CaseData.builder()
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .cmoToReviewList(hearingOrdersBundleId.toString())
            .reviewDraftOrdersData(reviewDraftOrdersData).build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, validateDecisionEventPath);

        assertThat(callbackResponse.getErrors()).containsOnly("Add what the LA needs to change on the draft order 2");
    }

    @Test
    void shouldReturnErrorsForAllReviewedOrdersWhenJudgeReviewIsInvalidForMoreThanOneDraftOrders() {
        UUID hearingOrdersBundleId = UUID.randomUUID();

        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            hearingOrdersBundleId, newArrayList(agreedCMO, draftOrder1, draftOrder2));

        ReviewDraftOrdersData reviewDraftOrdersData = ReviewDraftOrdersData.builder()
            .reviewDecision1(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .reviewDecision2(ReviewDecision.builder().decision(JUDGE_REQUESTED_CHANGES).build())
            .build();

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .reviewCMODecision(ReviewDecision.builder().decision(JUDGE_AMENDS_DRAFT).build())
            .cmoToReviewList(hearingOrdersBundleId.toString())
            .reviewDraftOrdersData(reviewDraftOrdersData).build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, validateDecisionEventPath);

        assertThat(callbackResponse.getErrors())
            .containsExactlyInAnyOrder("Add the new CMO", "Add what the LA needs to change on the draft order 2");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotReturnErrorsWhenJudgeDoesNotReviewOneOfTheDraftOrdersInTheBundle(boolean servingOthersToggledOn) {
        UUID hearingOrdersBundleId = UUID.randomUUID();

        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            hearingOrdersBundleId, newArrayList(draftOrder1, draftOrder2));

        ReviewDraftOrdersData reviewDraftOrdersData = ReviewDraftOrdersData.builder()
            .reviewDecision2(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build())
            .build();

        CaseData caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(Other.builder().name("test1").build())
                .additionalOthers(wrapElements(Other.builder().name("test2").build()))
                .build())
            .draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .cmoToReviewList(hearingOrdersBundleId.toString())
            .reviewDraftOrdersData(reviewDraftOrdersData).build();

        given(featureToggleService.isServeOrdersAndDocsToOthersEnabled()).willReturn(servingOthersToggledOn);
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, validateDecisionEventPath);

        assertThat(callbackResponse.getErrors()).isEmpty();
        if (servingOthersToggledOn) {
            assertThat(String.valueOf(callbackResponse.getData().get("hasOthers"))).isEqualTo("Yes");
            assertThat(String.valueOf(callbackResponse.getData().get("reviewCMOShowOthers"))).isEqualTo("Yes");
            assertThat(String.valueOf(callbackResponse.getData().get("others_label")))
                .contains("Other 1: test1", "Other 2: test2");
            assertThat(extractCaseData(callbackResponse).getOthersSelector()).isEqualTo(Selector.newSelector(2));
        } else {
            assertThat(callbackResponse.getData())
                .doesNotContainKeys("hasOthers", "others_label", "reviewCMOShowOthers")
                .containsEntry("othersSelector", null);
        }
    }

    @Test
    void shouldNotReturnErrorsWhenReviewDecisionForTheDraftOrdersIsValid() {
        UUID hearingOrdersBundleId = UUID.randomUUID();

        Element<HearingOrdersBundle> hearingOrdersBundle = buildHearingOrdersBundle(
            hearingOrdersBundleId, newArrayList(agreedCMO));

        CaseData caseData = CaseData.builder()
            .draftUploadedCMOs(newArrayList(agreedCMO))
            .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundle))
            .cmoToReviewList(hearingOrdersBundleId.toString())
            .reviewCMODecision(ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build()).build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, validateDecisionEventPath);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    private Element<HearingOrdersBundle> buildHearingOrdersBundle(
        UUID hearingOrdersBundle1, List<Element<HearingOrder>> orders) {
        return element(hearingOrdersBundle1,
            HearingOrdersBundle.builder().hearingId(UUID.randomUUID())
                .orders(orders)
                .hearingName(hearing).build());
    }

    private HearingOrder buildDraftOrder(HearingOrderType orderType) {
        return HearingOrder.builder()
            .hearing(hearing)
            .title(hearing)
            .order(order)
            .type(orderType)
            .status(DRAFT_CMO.equals(orderType) ? CMOStatus.DRAFT : SEND_TO_JUDGE).build();
    }
}
