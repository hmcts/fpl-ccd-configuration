package uk.gov.hmcts.reform.fpl.service.cmo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisApprovedOrderCoverSheetService;
import uk.gov.hmcts.reform.fpl.service.orders.generator.DocumentMerger;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.JUDGE_AMENDS_DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
class HearingOrderGeneratorTest {

    private static final DocumentReference order = testDocumentReference();
    private static final DocumentReference sealedOrder = testDocumentReference();
    private static final byte[] ORDER_WITH_COVER_SHEET_BYTES = new byte[]{1,2,3};
    private static final DocmosisDocument DOCMOSIS_DOCUMENT_ORDER_WITH_COVER_SHEET = DocmosisDocument.builder()
        .bytes(ORDER_WITH_COVER_SHEET_BYTES)
        .build();
    private static final Document ORDER_WITH_COVER_SHEET_DOCUMENT = testDocument();
    private static final DocumentReference ORDER_WITH_COVER_SHEET = DocumentReference
        .buildFromDocument(ORDER_WITH_COVER_SHEET_DOCUMENT);
    private static final DocumentReference amendedOrder = testDocumentReference();

    private static final Time time = new FixedTimeConfiguration().stoppedTime();

    public static final UUID ORDER_ID = UUID.randomUUID();

    @Mock
    private DocumentSealingService documentSealingService;
    @Mock
    private DocmosisApprovedOrderCoverSheetService docmosisApprovedOrderCoverSheetService;
    @Mock
    private DocumentMerger documentMerger;
    @Mock
    private UploadDocumentService uploadDocumentService;

    @InjectMocks
    private HearingOrderGenerator underTest;

    @BeforeEach
    void setUp() {
        underTest = new HearingOrderGenerator(documentSealingService, time, docmosisApprovedOrderCoverSheetService,
            documentMerger, uploadDocumentService);
    }

    @Test
    void shouldBuildSealedHearingOrderWhenReviewDecisionIsApproved() throws IOException {
        HearingOrder hearingOrder = HearingOrder.builder().hearing("hearing1").order(order).build();
        String othersNotified = "John Smith";
        List<Element<Other>> selectedOthers = List.of(element(Other.builder().name(othersNotified).build()));
        Court court = Court.builder().build();
        ReviewDecision reviewDecision = ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build();

        CaseData caseData = CaseData.builder()
            .court(court)
            .reviewCMODecision(reviewDecision)
            .build();

        when(documentSealingService.sealDocument(order, court, SealType.ENGLISH)).thenReturn(sealedOrder);
        when(docmosisApprovedOrderCoverSheetService.addCoverSheetToApprovedOrder(caseData, sealedOrder))
            .thenReturn(DOCMOSIS_DOCUMENT_ORDER_WITH_COVER_SHEET);
        when(uploadDocumentService.uploadPDF(eq(ORDER_WITH_COVER_SHEET_BYTES), any()))
            .thenReturn(ORDER_WITH_COVER_SHEET_DOCUMENT);

        Element<HearingOrder> expectedOrder = element(ORDER_ID, hearingOrder.toBuilder()
            .dateIssued(time.now().toLocalDate()).status(CMOStatus.APPROVED)
            .othersNotified(othersNotified)
            .others(selectedOthers)
            .order(ORDER_WITH_COVER_SHEET).lastUploadedOrder(order).build());

        Element<HearingOrder> actual = underTest.buildSealedHearingOrder(
            caseData,
            reviewDecision,
            element(ORDER_ID, hearingOrder),
            selectedOthers,
            othersNotified,
            true);

        assertThat(actual).isEqualTo(expectedOrder);
    }

    @Test
    void shouldBuildSealedHearingOrderWhenJudgeAmendsTheDocument() throws IOException {
        HearingOrder hearingOrder = HearingOrder.builder().hearing("hearing1").order(order).build();
        Court court = Court.builder().build();
        ReviewDecision reviewDecision = ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build();

        CaseData caseData = CaseData.builder()
            .court(court)
            .reviewCMODecision(reviewDecision)
            .build();
        when(documentSealingService.sealDocument(amendedOrder, court, SealType.ENGLISH)).thenReturn(sealedOrder);
        when(docmosisApprovedOrderCoverSheetService.addCoverSheetToApprovedOrder(caseData, sealedOrder))
            .thenReturn(DOCMOSIS_DOCUMENT_ORDER_WITH_COVER_SHEET);
        when(uploadDocumentService.uploadPDF(eq(ORDER_WITH_COVER_SHEET_BYTES), any()))
            .thenReturn(ORDER_WITH_COVER_SHEET_DOCUMENT);

        Element<HearingOrder> expectedOrder = element(ORDER_ID, hearingOrder.toBuilder()
            .dateIssued(time.now().toLocalDate()).status(CMOStatus.APPROVED)
            .others(List.of()).othersNotified("")
            .order(ORDER_WITH_COVER_SHEET).lastUploadedOrder(amendedOrder).build());

        Element<HearingOrder> actual = underTest.buildSealedHearingOrder(
            caseData,
            ReviewDecision.builder().decision(JUDGE_AMENDS_DRAFT).judgeAmendedDocument(amendedOrder).build(),
            element(ORDER_ID, hearingOrder),
            List.of(), "", true);

        assertThat(actual).isEqualTo(expectedOrder);
    }

    @Test
    void shouldBuildSealedHearingOrderWithoutCoverSheetIfNotRequired() {
        HearingOrder hearingOrder = HearingOrder.builder().hearing("hearing1").order(order).build();
        String othersNotified = "John Smith";
        List<Element<Other>> selectedOthers = List.of(element(Other.builder().name(othersNotified).build()));
        Court court = Court.builder().build();
        ReviewDecision reviewDecision = ReviewDecision.builder().decision(SEND_TO_ALL_PARTIES).build();

        CaseData caseData = CaseData.builder()
            .court(court)
            .reviewCMODecision(reviewDecision)
            .build();

        when(documentSealingService.sealDocument(order, court, SealType.ENGLISH)).thenReturn(sealedOrder);

        Element<HearingOrder> expectedOrder = element(ORDER_ID, hearingOrder.toBuilder()
            .dateIssued(time.now().toLocalDate()).status(CMOStatus.APPROVED)
            .othersNotified(othersNotified)
            .others(selectedOthers)
            .order(sealedOrder).lastUploadedOrder(order).build());

        Element<HearingOrder> actual = underTest.buildSealedHearingOrder(
            caseData,
            reviewDecision,
            element(ORDER_ID, hearingOrder),
            selectedOthers,
            othersNotified,
            false);

        assertThat(actual).isEqualTo(expectedOrder);
    }

    @Test
    void shouldBuildRejectedHearingOrderWhenJudgeRequestsChanges() {
        HearingOrder hearingOrder = HearingOrder.builder().hearing("hearing1").order(order).build();

        String changesRequested = "incorrect order";

        Element<HearingOrder> actual = underTest.buildRejectedHearingOrder(
            element(ORDER_ID, hearingOrder), changesRequested);

        assertThat(actual).isEqualTo(element(ORDER_ID, hearingOrder.toBuilder()
            .status(CMOStatus.RETURNED).requestedChanges(changesRequested).build()));
    }
}
