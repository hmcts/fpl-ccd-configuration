package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class HearingOrderGeneratorTest {

    private static final DocumentReference order = testDocumentReference();
    private static final DocumentReference sealedOrder = testDocumentReference();
    private static final DocumentReference amendedOrder = testDocumentReference();

    private static final Time time = new FixedTimeConfiguration().stoppedTime();

    public static final UUID ORDER_ID = UUID.randomUUID();

    @Mock
    private DocumentSealingService documentSealingService;

    @InjectMocks
    private HearingOrderGenerator underTest;

    @BeforeEach
    void setUp() {
        underTest = new HearingOrderGenerator(documentSealingService, time);
    }

    @Test
    void shouldBuildSealedHearingOrderWhenReviewDecisionIsApproved() {
        HearingOrder hearingOrder = HearingOrder.builder().hearing("hearing1").order(order).build();
        String othersNotified = "John Smith";
        List<Element<Other>> selectedOthers = List.of(element(Other.builder().name(othersNotified).build()));
        Court court = Court.builder().build();
        when(documentSealingService.sealDocument(order, court, SealType.ENGLISH)).thenReturn(sealedOrder);

        Element<HearingOrder> expectedOrder = element(ORDER_ID, hearingOrder.toBuilder()
            .dateIssued(time.now().toLocalDate()).status(CMOStatus.APPROVED)
            .othersNotified(othersNotified)
            .others(selectedOthers)
            .order(sealedOrder).lastUploadedOrder(order).build());

        Element<HearingOrder> actual = underTest.buildSealedHearingOrder(
            ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build(),
            element(ORDER_ID, hearingOrder),
            selectedOthers,
            othersNotified,
            SealType.ENGLISH,
            court);

        assertThat(actual).isEqualTo(expectedOrder);
    }

    @Test
    void shouldBuildSealedHearingOrderWhenJudgeAmendsTheDocument() {
        HearingOrder hearingOrder = HearingOrder.builder().hearing("hearing1").order(order).build();
        Court court = Court.builder().build();
        when(documentSealingService.sealDocument(amendedOrder, court, SealType.ENGLISH)).thenReturn(sealedOrder);

        Element<HearingOrder> expectedOrder = element(ORDER_ID, hearingOrder.toBuilder()
            .dateIssued(time.now().toLocalDate()).status(CMOStatus.APPROVED)
            .others(List.of()).othersNotified("")
            .order(sealedOrder).lastUploadedOrder(amendedOrder).build());

        Element<HearingOrder> actual = underTest.buildSealedHearingOrder(
            ReviewDecision.builder().decision(JUDGE_AMENDS_DRAFT).judgeAmendedDocument(amendedOrder).build(),
            element(ORDER_ID, hearingOrder),
            List.of(), "",
            SealType.ENGLISH,
            court);

        assertThat(actual).isEqualTo(expectedOrder);
    }

    @Test
    void shouldBuildRejectedHearingOrderWhenJudgeRequestsChanges() {
        HearingOrder hearingOrder = HearingOrder.builder().hearing("hearing1").order(order).build();

        String changesRequested = "incorrect order";

        Element<HearingOrder> actual = underTest.buildRejectedHearingOrder(
            element(ORDER_ID, hearingOrder), changesRequested);

        assertThat(actual).isEqualTo(element(ORDER_ID, hearingOrder.toBuilder()
            .status(CMOStatus.RETURNED).requestedChanges(changesRequested)
            .refusedOrder(hearingOrder.getOrder()).order(null).build()));
    }
}
