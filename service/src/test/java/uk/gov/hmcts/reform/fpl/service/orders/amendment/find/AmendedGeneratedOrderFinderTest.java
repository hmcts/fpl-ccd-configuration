package uk.gov.hmcts.reform.fpl.service.orders.amendment.find;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class AmendedGeneratedOrderFinderTest {
    private static final GeneratedOrder ORIGINAL_ORDER = mock(GeneratedOrder.class);
    private static final GeneratedOrder AMENDED_ORDER = mock(GeneratedOrder.class);
    private static final Element<GeneratedOrder> ORIGINAL_ELEMENT = element(ORIGINAL_ORDER);
    private static final Element<GeneratedOrder> AMENDED_ELEMENT = element(AMENDED_ORDER);

    private final CaseData caseData = mock(CaseData.class);
    private final CaseData caseDataBefore = mock(CaseData.class);

    private final AmendedGeneratedOrderFinder underTest = new AmendedGeneratedOrderFinder();

    @Test
    void findOrderIfPresentAmended() {
        when(caseData.getOrderCollection()).thenReturn(List.of(AMENDED_ELEMENT));
        when(caseDataBefore.getOrderCollection()).thenReturn(List.of(ORIGINAL_ELEMENT));

        assertThat(underTest.findOrderIfPresent(caseData, caseDataBefore)).contains(AMENDED_ORDER);
    }

    @Test
    void findOrderIfPresentNotAmended() {
        when(caseData.getOrderCollection()).thenReturn(List.of(ORIGINAL_ELEMENT));
        when(caseDataBefore.getOrderCollection()).thenReturn(List.of(ORIGINAL_ELEMENT));

        assertThat(underTest.findOrderIfPresent(caseData, caseDataBefore)).isEmpty();
    }
}
