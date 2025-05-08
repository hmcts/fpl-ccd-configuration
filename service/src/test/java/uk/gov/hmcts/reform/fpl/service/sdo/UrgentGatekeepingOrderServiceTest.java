package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.service.CourtLevelAllocationService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTime;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;

class UrgentGatekeepingOrderServiceTest {
    private static final DocumentReference UPLOADED_ORDER = mock(DocumentReference.class);
    private static final DocumentReference SEALED_ORDER = mock(DocumentReference.class);

    private final Time time = new FixedTime();
    private final DocumentSealingService sealingService = mock(DocumentSealingService.class);
    private final CourtLevelAllocationService allocationService = mock(CourtLevelAllocationService.class);
    private static final Court court = Court.builder().build();

    private UrgentGatekeepingOrderService underTest;

    @BeforeEach
    void setUp() {
        underTest = new UrgentGatekeepingOrderService(allocationService, sealingService, time);
        when(sealingService.sealDocument(UPLOADED_ORDER, court, SealType.ENGLISH)).thenReturn(SEALED_ORDER);
    }

    @Test
    void prePopulateHasAllocationDecision() {
        CaseData caseData = CaseData.builder().allocationDecision(mock(Allocation.class)).build();
        assertThat(underTest.prePopulate(caseData)).isEqualTo(GatekeepingOrderEventData.builder()
            .showUrgentHearingAllocation(YesNo.NO)
            .build()
        );
    }

    @Test
    void prePopulateHasNoAllocationDecision() {
        CaseData caseData = mock(CaseData.class);
        Allocation preparedAllocation = mock(Allocation.class);

        when(allocationService.createDecision(caseData)).thenReturn(preparedAllocation);

        assertThat(underTest.prePopulate(caseData)).isEqualTo(GatekeepingOrderEventData.builder()
            .urgentHearingAllocation(preparedAllocation)
            .showUrgentHearingAllocation(YesNo.YES)
            .build()
        );
    }

    @Test
    void finaliseWithPrePreparedAllocationDecision() {
        CaseData caseData = CaseData.builder()
            .court(court)
            .allocationDecision(mock(Allocation.class))
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .urgentHearingOrderDocument(UPLOADED_ORDER)
                .showUrgentHearingAllocation(YesNo.NO)
                .build())
            .build();

        UrgentHearingOrder expectedOrder = UrgentHearingOrder.builder()
            .order(UPLOADED_ORDER)
            .unsealedOrder(UPLOADED_ORDER)
            .dateAdded(time.now().toLocalDate())
            .build();

        assertThat(underTest.finalise(caseData)).isEqualTo(Map.of(
            "urgentHearingOrder", expectedOrder
        ));
    }

    @Test
    void finaliseWithPrePreparedAllocationDecisionAndTranslation() {
        CaseData caseData = CaseData.builder()
            .court(court)
            .allocationDecision(mock(Allocation.class))
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .urgentHearingOrderDocument(UPLOADED_ORDER)
                .showUrgentHearingAllocation(YesNo.NO)
                .urgentGatekeepingTranslationRequirements(ENGLISH_TO_WELSH)
                .build())
            .build();

        UrgentHearingOrder expectedOrder = UrgentHearingOrder.builder()
            .order(UPLOADED_ORDER)
            .unsealedOrder(UPLOADED_ORDER)
            .dateAdded(time.now().toLocalDate())
            .translationRequirements(ENGLISH_TO_WELSH)
            .build();

        assertThat(underTest.finalise(caseData)).isEqualTo(Map.of(
            "urgentHearingOrder", expectedOrder
        ));
    }

    @Test
    void finaliseWithNoPrePreparedAllocationDecision() {
        Allocation enteredAllocation = mock(Allocation.class);
        Allocation updatedAllocation = mock(Allocation.class);

        CaseData caseData = CaseData.builder()
            .court(court)
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .urgentHearingAllocation(enteredAllocation)
                .urgentHearingOrderDocument(UPLOADED_ORDER)
                .showUrgentHearingAllocation(YesNo.NO)
                .build())
            .build();

        when(allocationService.createAllocationDecisionIfNull(caseData, enteredAllocation))
            .thenReturn(updatedAllocation);
        when(updatedAllocation.getProposalV2()).thenReturn("some allocation level");

        UrgentHearingOrder expectedOrder = UrgentHearingOrder.builder()
            .order(UPLOADED_ORDER)
            .unsealedOrder(UPLOADED_ORDER)
            .allocation("some allocation level")
            .dateAdded(time.now().toLocalDate())
            .build();

        assertThat(underTest.finalise(caseData)).isEqualTo(Map.of(
            "urgentHearingOrder", expectedOrder,
            "allocationDecision", updatedAllocation
        ));
    }

    @Test
    void getNoticeOfProceedingsTemplatesWithNoOthers() {
        CaseData caseData = CaseData.builder().build();

        assertThat(underTest.getNoticeOfProceedingsTemplates(caseData)).isEqualTo(List.of(C6));
    }

    @Test
    void getNoticeOfProceedingsTemplatesWithOthers() {
        CaseData caseData = CaseData.builder().others(Others.builder().firstOther(mock(Other.class)).build()).build();

        assertThat(underTest.getNoticeOfProceedingsTemplates(caseData)).isEqualTo(List.of(C6, C6A));
    }

    @Test
    void sealDocumentAfterEventSubmitted() {
        UrgentHearingOrder order = UrgentHearingOrder.builder()
            .order(UPLOADED_ORDER)
            .unsealedOrder(UPLOADED_ORDER)
            .dateAdded(time.now().toLocalDate())
            .translationRequirements(ENGLISH_TO_WELSH)
            .build();

        CaseData caseData = CaseData.builder()
            .court(court)
            .allocationDecision(mock(Allocation.class))
            .urgentHearingOrder(order)
            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
                .urgentHearingOrderDocument(UPLOADED_ORDER)
                .showUrgentHearingAllocation(YesNo.NO)
                .urgentGatekeepingTranslationRequirements(ENGLISH_TO_WELSH)
                .build())
            .build();

        underTest.sealDocumentAfterEventSubmitted(caseData);

        verify(sealingService).sealDocument(UPLOADED_ORDER, court, SealType.ENGLISH);
    }
}
