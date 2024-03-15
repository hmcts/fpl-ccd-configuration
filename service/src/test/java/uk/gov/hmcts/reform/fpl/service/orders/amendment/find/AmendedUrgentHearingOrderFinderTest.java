package uk.gov.hmcts.reform.fpl.service.orders.amendment.find;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AmendedUrgentHearingOrderFinderTest {
    private static final UrgentHearingOrder ORIGINAL_UHO = mock(UrgentHearingOrder.class);
    private static final UrgentHearingOrder AMENDED_UHO = mock(UrgentHearingOrder.class);

    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);

    private final AmendedUrgentHearingOrderFinder underTest = new AmendedUrgentHearingOrderFinder();

    @Test
    void findOrderIfPresentAmended() {
        when(caseData.getUrgentHearingOrder()).thenReturn(AMENDED_UHO.toBuilder()
            .order(mock(DocumentReference.class)).build());
        when(caseDataBefore.getUrgentHearingOrder()).thenReturn(ORIGINAL_UHO.toBuilder()
            .order(mock(DocumentReference.class)).build());

        assertThat(underTest.findOrderIfPresent(caseData, caseDataBefore)).contains(AMENDED_UHO);
    }

    @Test
    void findOrderIfPresentNotAmended() {
        DocumentReference order = mock(DocumentReference.class);
        when(caseData.getUrgentHearingOrder()).thenReturn(AMENDED_UHO.toBuilder()
            .order(order).build());
        when(caseDataBefore.getUrgentHearingOrder()).thenReturn(ORIGINAL_UHO.toBuilder()
            .order(order).build());

        assertThat(underTest.findOrderIfPresent(caseData, caseDataBefore)).isEmpty();
    }
}
