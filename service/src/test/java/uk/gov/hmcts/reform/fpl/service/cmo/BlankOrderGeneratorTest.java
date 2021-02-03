package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class BlankOrderGeneratorTest {

    private static final String hearing1 = "Case management hearing, 2 March 2020";
    private static final DocumentReference order = testDocumentReference();
    private static final Time TIME = new FixedTimeConfiguration().stoppedTime();

    private final BlankOrderGenerator underTest = new BlankOrderGenerator(TIME);

    @Test
    void testShouldCreateBlankOrderFromC21Draft() {
        DocumentReference amendedDocument = testDocumentReference();

        Element<HearingOrder> draftOrder1 = buildBlankOrder("test order1", hearing1);

        Element<HearingOrdersBundle> ordersBundleElement = buildDraftOrdersBundle(hearing1,
            newArrayList(draftOrder1));

        Element<GeneratedOrder> actual = underTest.buildBlankOrder(CaseData.builder()
                .state(State.CASE_MANAGEMENT)
                .draftUploadedCMOs(newArrayList(draftOrder1))
                .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
                .reviewCMODecision(ReviewDecision.builder()
                    .decision(JUDGE_AMENDS_DRAFT)
                    .judgeAmendedDocument(amendedDocument)
                    .build())
                .orderCollection(newArrayList())
                .build(),
            ordersBundleElement,
            draftOrder1);

        assertThat(actual.getValue()).isEqualTo(expectedBlankOrder("test order1"));
    }

    private static Element<HearingOrder> buildBlankOrder(String title, String hearing) {
        return element(HearingOrder.builder()
            .hearing(hearing)
            .title(title)
            .order(order)
            .type(HearingOrderType.C21)
            .status(SEND_TO_JUDGE)
            .dateIssued(TIME.now().toLocalDate())
            .judgeTitleAndName("Her Honour Judge Judy").build());
    }

    private static Element<HearingOrdersBundle> buildDraftOrdersBundle(
        String hearing, List<Element<HearingOrder>> draftOrders) {
        return element(HearingOrdersBundle.builder()
            .hearingName(hearing)
            .orders(draftOrders)
            .judgeTitleAndName("Her Honour Judge Judy").build());
    }

    private GeneratedOrder expectedBlankOrder(String title) {
        return GeneratedOrder.builder()
            .type(BLANK_ORDER.getLabel())
            .title(title)
            .document(order)
            .dateOfIssue(formatLocalDateToString(TIME.now().toLocalDate(), DATE))
            .children(emptyList())
            .date(formatLocalDateTimeBaseUsingFormat(TIME.now(), TIME_DATE))
            .build();
    }
}
