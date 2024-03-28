package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class DraftOrdersReviewDataBuilderTest {

    private static final String CMO_TITLE = "CMO title";
    private static final String BLANK_ORDER_TITLE_1 = "BLANK order title 1";
    private static final String BLANK_ORDER_TITLE_2 = "BLANK order title 2";
    private static final DocumentReference DOCUMENT_REFERENCE = mock(DocumentReference.class);
    private static final DocumentReference DOCUMENT_REFERENCE_1 = mock(DocumentReference.class);
    private static final DocumentReference DOCUMENT_REFERENCE_2 = mock(DocumentReference.class);
    private static final CMOStatus NON_TO_APPROVE_STATUS = CMOStatus.APPROVED;
    private static final String HEARING_NAME = "Hearing name";
    private final DraftOrdersReviewDataBuilder underTest = new DraftOrdersReviewDataBuilder();

    @Test
    void testIfNoOrders() {
        Map<String, Object> actual = underTest.buildDraftOrdersReviewData(HearingOrdersBundle.builder().build());

        assertThat(actual).isEqualTo(Map.of(
            "draftOrdersTitlesInBundle","",
            "draftCMOExists", "N"
        ));
    }

    @Test
    void testIfOnlyOneOrderNotToBeApproved() {
        List<Element<HearingOrder>> orders = new ArrayList<>();
        orders.add(anOrder(HearingOrderType.DRAFT_CMO, NON_TO_APPROVE_STATUS, CMO_TITLE, DOCUMENT_REFERENCE));

        Map<String, Object> actual = underTest.buildDraftOrdersReviewData(HearingOrdersBundle.builder()
            .orders(orders)
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "draftOrdersTitlesInBundle","",
            "draftCMOExists", "N"
        ));
    }

    @Test
    void testIfOnlyOneCMOToBeApproved() {
        List<Element<HearingOrder>> orders = new ArrayList<>();
        orders.add(anOrder(HearingOrderType.DRAFT_CMO, CMOStatus.SEND_TO_JUDGE, CMO_TITLE, DOCUMENT_REFERENCE));

        Map<String, Object> actual = underTest.buildDraftOrdersReviewData(HearingOrdersBundle.builder()
            .orders(orders)
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "draftOrdersTitlesInBundle","CMO",
            "draftCMOExists", "Y",
            "cmoDraftOrderTitle", CMO_TITLE,
            "cmoDraftOrderDocument", DOCUMENT_REFERENCE,
            "reviewCMODecision", ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build()
        ));
    }

    @Test
    void testIfOnlyOneCMOToBeApprovedWithHearing() {
        List<Element<HearingOrder>> orders = new ArrayList<>();
        orders.add(anOrder(HearingOrderType.DRAFT_CMO, CMOStatus.SEND_TO_JUDGE, CMO_TITLE, DOCUMENT_REFERENCE));

        Map<String, Object> actual = underTest.buildDraftOrdersReviewData(HearingOrdersBundle.builder()
            .hearingId(UUID.randomUUID())
            .hearingName(HEARING_NAME)
            .orders(orders)
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "draftOrdersTitlesInBundle","CMO for Hearing name",
            "draftCMOExists", "Y",
            "cmoDraftOrderTitle", CMO_TITLE,
            "cmoDraftOrderDocument", DOCUMENT_REFERENCE,
            "reviewCMODecision", ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build()
        ));
    }

    @Test
    void testIfOnlyOneBlankOrderToBeApproved() {
        List<Element<HearingOrder>> orders = new ArrayList<>();
        orders.add(anOrder(HearingOrderType.C21, CMOStatus.SEND_TO_JUDGE, BLANK_ORDER_TITLE_1, DOCUMENT_REFERENCE));

        Map<String, Object> actual = underTest.buildDraftOrdersReviewData(HearingOrdersBundle.builder()
            .orders(orders)
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "draftOrdersTitlesInBundle","C21 Order",
            "draftCMOExists", "N",
            "draftBlankOrdersCount", "1",
            "draftOrder1Document", DOCUMENT_REFERENCE,
            "draftOrder1Title", BLANK_ORDER_TITLE_1,
            "reviewDecision1", ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build()
        ));
    }

    @Test
    void testIfOnlyOneBlankOrderToBeApprovedWithHearing() {
        List<Element<HearingOrder>> orders = new ArrayList<>();
        orders.add(anOrder(HearingOrderType.C21, CMOStatus.SEND_TO_JUDGE, BLANK_ORDER_TITLE_1, DOCUMENT_REFERENCE));

        Map<String, Object> actual = underTest.buildDraftOrdersReviewData(HearingOrdersBundle.builder()
            .hearingId(UUID.randomUUID())
            .hearingName(HEARING_NAME)
            .orders(orders)
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "draftOrdersTitlesInBundle","C21 Order - Hearing name",
            "draftCMOExists", "N",
            "draftBlankOrdersCount", "1",
            "draftOrder1Document", DOCUMENT_REFERENCE,
            "draftOrder1Title", BLANK_ORDER_TITLE_1,
            "reviewDecision1", ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build()
        ));
    }

    @Test
    void testIfOnlyMoreThanOneBlankOrderToBeApproved() {
        List<Element<HearingOrder>> orders = new ArrayList<>();
        orders.add(anOrder(HearingOrderType.C21, CMOStatus.SEND_TO_JUDGE, BLANK_ORDER_TITLE_1, DOCUMENT_REFERENCE));
        orders.add(anOrder(HearingOrderType.C21, CMOStatus.SEND_TO_JUDGE, BLANK_ORDER_TITLE_2, DOCUMENT_REFERENCE_2));

        Map<String, Object> actual = underTest.buildDraftOrdersReviewData(HearingOrdersBundle.builder()
            .orders(orders)
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "draftOrdersTitlesInBundle","C21 Order\nC21 Order",
            "draftCMOExists", "N",
            "draftBlankOrdersCount", "12",
            "draftOrder1Document", DOCUMENT_REFERENCE,
            "draftOrder1Title", BLANK_ORDER_TITLE_1,
            "draftOrder2Document", DOCUMENT_REFERENCE_2,
            "draftOrder2Title", BLANK_ORDER_TITLE_2,
            "reviewDecision1", ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build(),
            "reviewDecision2", ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build()
        ));
    }

    @Test
    void testIfOnlyMoreThanOneBlankOrderToBeApprovedAndCMO() {
        List<Element<HearingOrder>> orders = new ArrayList<>();
        orders.add(anOrder(HearingOrderType.DRAFT_CMO, CMOStatus.SEND_TO_JUDGE, CMO_TITLE, DOCUMENT_REFERENCE));
        orders.add(anOrder(HearingOrderType.C21, CMOStatus.SEND_TO_JUDGE, BLANK_ORDER_TITLE_1, DOCUMENT_REFERENCE_1));
        orders.add(anOrder(HearingOrderType.C21, CMOStatus.SEND_TO_JUDGE, BLANK_ORDER_TITLE_2, DOCUMENT_REFERENCE_2));

        Map<String, Object> expected = new HashMap<>();
        expected.putAll(Map.of(
            "draftOrdersTitlesInBundle","CMO\nC21 Order\nC21 Order",
            "draftCMOExists", "Y",
            "cmoDraftOrderTitle", CMO_TITLE,
            "cmoDraftOrderDocument", DOCUMENT_REFERENCE,
            "draftBlankOrdersCount", "12",
            "draftOrder1Document", DOCUMENT_REFERENCE_1,
            "draftOrder1Title", BLANK_ORDER_TITLE_1,
            "draftOrder2Document", DOCUMENT_REFERENCE_2,
            "draftOrder2Title", BLANK_ORDER_TITLE_2
        ));
        expected.put("reviewCMODecision", ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build());
        expected.put("reviewDecision1", ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build());
        expected.put("reviewDecision2", ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build());

        Map<String, Object> actual = underTest.buildDraftOrdersReviewData(HearingOrdersBundle.builder()
            .orders(orders)
            .build());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testIfOnlyMoreThanOneBlankOrderToBeApprovedAndCMOWithHearing() {
        List<Element<HearingOrder>> orders = new ArrayList<>();
        orders.add(anOrder(HearingOrderType.DRAFT_CMO, CMOStatus.SEND_TO_JUDGE, CMO_TITLE, DOCUMENT_REFERENCE));
        orders.add(anOrder(HearingOrderType.C21, CMOStatus.SEND_TO_JUDGE, BLANK_ORDER_TITLE_1, DOCUMENT_REFERENCE_1));
        orders.add(anOrder(HearingOrderType.C21, CMOStatus.SEND_TO_JUDGE, BLANK_ORDER_TITLE_2, DOCUMENT_REFERENCE_2));

        Map<String, Object> expected = new HashMap<>();
        expected.putAll(Map.of(
            "draftOrdersTitlesInBundle","CMO for Hearing name\nC21 Order - Hearing name\nC21 Order - Hearing name",
            "draftCMOExists", "Y",
            "cmoDraftOrderTitle", CMO_TITLE,
            "cmoDraftOrderDocument", DOCUMENT_REFERENCE,
            "draftBlankOrdersCount", "12",
            "draftOrder1Document", DOCUMENT_REFERENCE_1,
            "draftOrder1Title", BLANK_ORDER_TITLE_1,
            "draftOrder2Document", DOCUMENT_REFERENCE_2,
            "draftOrder2Title", BLANK_ORDER_TITLE_2
        ));
        expected.put("reviewCMODecision", ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build());
        expected.put("reviewDecision1", ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build());
        expected.put("reviewDecision2", ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build());

        Map<String, Object> actual = underTest.buildDraftOrdersReviewData(HearingOrdersBundle.builder()
            .hearingId(UUID.randomUUID())
            .hearingName(HEARING_NAME)
            .orders(orders)
            .build());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testIfConfidentialOrderToBeApproved() {
        List<Element<HearingOrder>> orders = new ArrayList<>();
        orders.add(anOrder(HearingOrderType.C21, CMOStatus.SEND_TO_JUDGE, BLANK_ORDER_TITLE_1, DOCUMENT_REFERENCE));

        Map<String, Object> actual = underTest.buildDraftOrdersReviewData(HearingOrdersBundle.builder()
            .ordersCTSC(orders)
            .build());

        assertThat(actual).isEqualTo(Map.of(
            "draftOrdersTitlesInBundle","C21 Order",
            "draftCMOExists", "N",
            "draftBlankOrdersCount", "1",
            "draftOrder1Document", DOCUMENT_REFERENCE,
            "draftOrder1Title", BLANK_ORDER_TITLE_1,
            "reviewDecision1", ReviewDecision.builder().decision(CMOReviewOutcome.REVIEW_LATER).build()
        ));
    }

    private Element<HearingOrder> anOrder(HearingOrderType c21, CMOStatus sendToJudge, String blankOrderTitle2,
                                          DocumentReference documentReference2) {
        return element(UUID.randomUUID(), HearingOrder.builder()
            .type(c21)
            .status(sendToJudge)
            .title(blankOrderTitle2)
            .order(documentReference2)
            .build());
    }
}
