package uk.gov.hmcts.reform.fpl.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.CourtLevelAllocationService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UrgentGatekeepingOrderServiceTest {
    private final static DocumentReference UPLOADED_ORDER = mock(DocumentReference.class);
    private final static DocumentReference SEALED_ORDER = mock(DocumentReference.class);

    private final Time time = new FixedTime();
    private final DocumentSealingService sealingService = mock(DocumentSealingService.class);
    private final CourtLevelAllocationService allocationService = mock(CourtLevelAllocationService.class);

//    private UrgentGatekeepingOrderService underTest;

    @BeforeEach
    void setUp() {
//        underTest = new UrgentGatekeepingOrderService(allocationService, sealingService, time);
    }

    @Test
    void prePopulateHasAllocationDecision() {
        CaseData caseData = CaseData.builder().allocationDecision(mock(Allocation.class)).build();
//        assertThat(underTest.prePopulate(caseData)).isEqualTo(GatekeepingOrderEventData.builder()
//            .showUrgentHearingAllocation(YesNo.NO)
//            .build()
//        );
    }

    @Test
    void prePopulateHasNoAllocationDecision() {
        CaseData caseData = mock(CaseData.class);
        Allocation preparedAllocation = mock(Allocation.class);

        when(allocationService.createDecision(caseData)).thenReturn(preparedAllocation);

//        assertThat(underTest.prePopulate(caseData)).isEqualTo(GatekeepingOrderEventData.builder()
//            .urgentHearingAllocation(preparedAllocation)
//            .showUrgentHearingAllocation(YesNo.YES)
//            .build()
//        );
    }

    @Test
    void finaliseWithPrePreparedAllocationDecision() {
        CaseData caseData = CaseData.builder()
            .allocationDecision(mock(Allocation.class))
//            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
//                .urgentHearingOrderDocument(UPLOADED_ORDER)
//                .showUrgentHearingAllocation(YesNo.NO)
//                .build())
            .build();

        when(sealingService.sealDocument(UPLOADED_ORDER)).thenReturn(SEALED_ORDER);

//        UrgentHearingOrder expectedOrder = UrgentHearingOrder.builder()
//            .order(SEALED_ORDER)
//            .unsealedOrder(UPLOADED_ORDER)
//            .dateAdded(time.now().toLocalDate())
//            .build();
//
//        assertThat(underTest.finalise(caseData)).isEqualTo(Map.of(
//            "urgentHearingOrder", expectedOrder
//        ));
    }

    @Test
    void finaliseWithNoPrePreparedAllocationDecision() {
        Allocation enteredAllocation = mock(Allocation.class);
        Allocation updatedAllocation = mock(Allocation.class);

        CaseData caseData = CaseData.builder()
//            .gatekeepingOrderEventData(GatekeepingOrderEventData.builder()
//                .urgentHearingAllocation(enteredAllocation)
//                .urgentHearingOrderDocument(UPLOADED_ORDER)
//                .showUrgentHearingAllocation(YesNo.NO)
//                .build())
            .build();

        when(sealingService.sealDocument(UPLOADED_ORDER)).thenReturn(SEALED_ORDER);
//        when(allocationService.setAllocationDecisionIfNull(caseData, enteredAllocation)).thenReturn(updatedAllocation);
//
//        UrgentHearingOrder expectedOrder = UrgentHearingOrder.builder()
//            .order(SEALED_ORDER)
//            .unsealedOrder(UPLOADED_ORDER)
//            .allocation(updatedAllocation)
//            .dateAdded(time.now().toLocalDate())
//            .build();
//
//        assertThat(underTest.finalise(caseData)).isEqualTo(Map.of(
//            "urgentHearingOrder", expectedOrder,
//            "allocationDecision", updatedAllocation
//        ));
    }
}
