package uk.gov.hmcts.reform.fpl.service.orders.amendment.list;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class AmendableCaseManagementOrderProviderTest {
    private final AmendableCaseManagementOrderProvider underTest = new AmendableCaseManagementOrderProvider();

    @Test
    void provideListItems() {
        CaseData caseData = mock(CaseData.class);
        List<Element<HearingOrder>> orders = wrapElements(mock(HearingOrder.class), mock(HearingOrder.class));
        when(caseData.getSealedCMOs()).thenReturn(orders);
        assertThat(underTest.provideListItems(caseData)).isEqualTo(orders);
    }
}
