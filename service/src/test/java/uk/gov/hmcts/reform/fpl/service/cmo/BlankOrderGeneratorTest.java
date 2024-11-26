package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class BlankOrderGeneratorTest {

    private static final String HEARING_1 = "Case management hearing, 2 March 2020";
    private static final DocumentReference order = testDocumentReference();
    private static final Time TIME = new FixedTimeConfiguration().stoppedTime();
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENTS = ENGLISH_TO_WELSH;

    private final BlankOrderGenerator underTest = new BlankOrderGenerator(TIME);
    public static final UUID HEARING_ID = UUID.randomUUID();

    @Test
    void testShouldCreateBlankOrderFromC21DraftForABundleWithHearing() {
        DocumentReference amendedDocument = testDocumentReference();

        Element<HearingOrder> draftOrder1 = buildBlankOrder(TIME.now().toLocalDate());

        Element<HearingOrdersBundle> ordersBundleElement = buildDraftOrdersBundle(
            newArrayList(draftOrder1), HEARING_ID);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = JudgeAndLegalAdvisor.builder()
            .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
            .judgeLastName("Moley")
            .build();
        String othersNotified = "John Smith";
        List<Element<Other>> selectedOthers = List.of(element(Other.builder().name(othersNotified).build()));

        Element<GeneratedOrder> actual = underTest.buildBlankOrder(CaseData.builder()
                .state(State.CASE_MANAGEMENT)
                .draftUploadedCMOs(newArrayList(draftOrder1))
                .hearingOrdersBundlesDrafts(newArrayList(ordersBundleElement))
                .hearingDetails(List.of(element(HEARING_ID, HearingBooking.builder()
                    .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
                    .build())))
                .reviewCMODecision(ReviewDecision.builder()
                    .decision(JUDGE_AMENDS_DRAFT)
                    .judgeAmendedDocument(amendedDocument)
                    .build())
                .orderCollection(newArrayList())
                .build(),
            ordersBundleElement,
            draftOrder1,
            selectedOthers,
            othersNotified);

        assertThat(actual.getValue()).isEqualTo(expectedBlankOrder(
            draftOrder1.getValue().getDateIssued(), judgeAndLegalAdvisor, selectedOthers, othersNotified));
    }

    @Test
    void testShouldCreateBlankOrderFromC21DraftForNoHearingBundle() {
        DocumentReference amendedDocument = testDocumentReference();

        Element<HearingOrder> draftOrder1 = buildBlankOrder(null);

        Element<HearingOrdersBundle> ordersBundleElement = buildDraftOrdersBundle(
            newArrayList(draftOrder1), null);

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
            draftOrder1,
            List.of(),
            EMPTY);

        assertThat(actual.getValue()).isEqualTo(expectedBlankOrder(
            draftOrder1.getValue().getDateIssued(), null, List.of(), EMPTY));
    }

    private static Element<HearingOrder> buildBlankOrder(LocalDate dateIssued) {
        return element(HearingOrder.builder()
            .hearing(HEARING_1)
            .title("test order1")
            .order(order)
            .type(HearingOrderType.C21)
            .status(SEND_TO_JUDGE)
            .dateIssued(dateIssued)
            .translationRequirements(TRANSLATION_REQUIREMENTS)
            .judgeTitleAndName("Her Honour Judge Judy").build());
    }

    private static Element<HearingOrdersBundle> buildDraftOrdersBundle(
        List<Element<HearingOrder>> draftOrders, UUID hearingId) {
        return element(HearingOrdersBundle.builder()
            .hearingName(HEARING_1)
            .hearingId(hearingId)
            .orders(draftOrders)
            .judgeTitleAndName("Her Honour Judge Judy").build());
    }

    private GeneratedOrder expectedBlankOrder(LocalDate dateIssued,
                                              JudgeAndLegalAdvisor judgeAndLegalAdvisor,
                                              List<Element<Other>> selectedOthers,
                                              String othersNotified) {
        return GeneratedOrder.builder()
            .type(BLANK_ORDER.getLabel())
            .title("test order1")
            .document(order)
            .dateOfIssue(dateIssued != null ? formatLocalDateToString(dateIssued, DATE) : null)
            .children(emptyList())
            .date(formatLocalDateTimeBaseUsingFormat(TIME.now(), TIME_DATE))
            .judgeAndLegalAdvisor(judgeAndLegalAdvisor)
            .othersNotified(othersNotified)
            .translationRequirements(TRANSLATION_REQUIREMENTS)
            .others(selectedOthers)
            .build();
    }
}
