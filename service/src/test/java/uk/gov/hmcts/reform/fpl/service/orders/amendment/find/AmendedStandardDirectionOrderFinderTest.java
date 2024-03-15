package uk.gov.hmcts.reform.fpl.service.orders.amendment.find;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AmendedStandardDirectionOrderFinderTest {
    private static final StandardDirectionOrder ORIGINAL_SDO = mock(StandardDirectionOrder.class);
    private static final StandardDirectionOrder AMENDED_SDO = mock(StandardDirectionOrder.class);

    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);

    private final AmendedStandardDirectionOrderFinder underTest = new AmendedStandardDirectionOrderFinder();

    @Test
    void findOrderIfPresentAmended() {
        when(caseData.getStandardDirectionOrder()).thenReturn(AMENDED_SDO.toBuilder()
            .orderDoc(mock(DocumentReference.class)).build());
        when(caseDataBefore.getStandardDirectionOrder()).thenReturn(ORIGINAL_SDO.toBuilder()
                .orderDoc(mock(DocumentReference.class)).build());

        assertThat(underTest.findOrderIfPresent(caseData, caseDataBefore)).contains(AMENDED_SDO);
    }

    @Test
    void findOrderIfPresentNotAmended() {
        DocumentReference order = mock(DocumentReference.class);
        when(caseData.getStandardDirectionOrder()).thenReturn(AMENDED_SDO.toBuilder()
            .orderDoc(order).build());
        when(caseDataBefore.getStandardDirectionOrder()).thenReturn(ORIGINAL_SDO.toBuilder()
            .orderDoc(order).build());

        assertThat(underTest.findOrderIfPresent(caseData, caseDataBefore)).isEmpty();
    }
}
