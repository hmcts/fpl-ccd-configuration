package uk.gov.hmcts.reform.fpl.service.orders.amendment.list;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class AmendableGeneratedOrderProviderTest {
    private final AmendableGeneratedOrderProvider underTest = new AmendableGeneratedOrderProvider();

    @Test
    void provideListItems() {
        CaseData caseData = mock(CaseData.class);
        List<Element<GeneratedOrder>> orders = wrapElements(mock(GeneratedOrder.class), mock(GeneratedOrder.class));
        when(caseData.getAllOrderCollections()).thenReturn(orders);
        assertThat(underTest.provideListItems(caseData)).isEqualTo(orders);
    }
}
