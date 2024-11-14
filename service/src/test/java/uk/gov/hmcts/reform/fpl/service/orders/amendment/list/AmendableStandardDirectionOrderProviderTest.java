package uk.gov.hmcts.reform.fpl.service.orders.amendment.list;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class AmendableStandardDirectionOrderProviderTest {
    private final CaseData caseData = mock(CaseData.class);

    private final AmendableStandardDirectionOrderProvider underTest = new AmendableStandardDirectionOrderProvider();

    @Test
    void provideListItemsSealed() {
        StandardDirectionOrder standardDirectionOrder = mock(StandardDirectionOrder.class);

        when(caseData.getStandardDirectionOrder()).thenReturn(standardDirectionOrder);
        when(standardDirectionOrder.isSealed()).thenReturn(true);

        assertThat(underTest.provideListItems(caseData)).isEqualTo(List.of(
            element(StandardDirectionOrder.COLLECTION_ID, standardDirectionOrder)
        ));
    }

    @Test
    void provideListItemsNotSealed() {
        StandardDirectionOrder standardDirectionOrder = mock(StandardDirectionOrder.class);

        when(caseData.getStandardDirectionOrder()).thenReturn(standardDirectionOrder);
        when(standardDirectionOrder.isSealed()).thenReturn(false);

        assertThat(underTest.provideListItems(caseData)).isEmpty();
    }

    @Test
    void provideListItemsNull() {
        when(caseData.getStandardDirectionOrder()).thenReturn(null);
        assertThat(underTest.provideListItems(caseData)).isEmpty();
    }
}
