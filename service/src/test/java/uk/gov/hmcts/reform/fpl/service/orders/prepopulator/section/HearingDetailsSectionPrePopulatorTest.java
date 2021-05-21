package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HearingDetailsSectionPrePopulatorTest {

    private final HearingDetailsSectionPrePopulator underTest = new HearingDetailsSectionPrePopulator();
    private final Order mockOrder = mock(Order.class);
    private static final String ORDER_NAME = "Mock order pls ignore";

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderSection.HEARING_DETAILS);
    }

    @Test
    void prePopulate() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder().manageOrdersType(mockOrder).build())
            .build();

        when(mockOrder.getHistoryTitle()).thenReturn(ORDER_NAME);

        assertThat(underTest.prePopulate(caseData)).isEqualTo(Map.of(
            "hearingDetailsSectionSubHeader", ORDER_NAME
        ));
    }
}
