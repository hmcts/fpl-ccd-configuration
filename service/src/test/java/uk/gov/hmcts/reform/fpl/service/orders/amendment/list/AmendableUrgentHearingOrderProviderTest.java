package uk.gov.hmcts.reform.fpl.service.orders.amendment.list;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class AmendableUrgentHearingOrderProviderTest {
    private final CaseData caseData = mock(CaseData.class);

    private final AmendableUrgentHearingOrderProvider underTest = new AmendableUrgentHearingOrderProvider();

    @Test
    void provideListItemsNonNull() {
        UrgentHearingOrder urgentHearingOrder = mock(UrgentHearingOrder.class);

        when(caseData.getUrgentHearingOrder()).thenReturn(urgentHearingOrder);

        assertThat(underTest.provideListItems(caseData)).isEqualTo(List.of(
            element(UrgentHearingOrder.COLLECTION_ID, urgentHearingOrder)
        ));
    }

    @Test
    void provideListItemsNull() {
        when(caseData.getUrgentHearingOrder()).thenReturn(null);
        assertThat(underTest.provideListItems(caseData)).isEmpty();
    }
}
