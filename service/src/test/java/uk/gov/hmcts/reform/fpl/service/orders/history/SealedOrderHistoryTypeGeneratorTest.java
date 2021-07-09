package uk.gov.hmcts.reform.fpl.service.orders.history;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.C43OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.generator.C43ChildArrangementOrderTitleGenerator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.CHILD_ARRANGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.PROHIBITED_STEPS_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.SPECIFIC_ISSUE_ORDER;

public class SealedOrderHistoryTypeGeneratorTest {
    private final C43ChildArrangementOrderTitleGenerator c43ChildArrangementOrderTitleGenerator = mock(
        C43ChildArrangementOrderTitleGenerator.class);
    private final SealedOrderHistoryTypeGenerator underTest = new SealedOrderHistoryTypeGenerator(
        c43ChildArrangementOrderTitleGenerator);

    @Test
    void testIfChildArrangementSpecificIssueProhibitedStepsOrder() {
        List<C43OrderType> orders = List.of(CHILD_ARRANGEMENT_ORDER, SPECIFIC_ISSUE_ORDER, PROHIBITED_STEPS_ORDER);

        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersType(Order.C43_CHILD_ARRANGEMENTS_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER)
            .manageOrdersMultiSelectListForC43(orders)
            .build();

        when(c43ChildArrangementOrderTitleGenerator.getOrderTitle(manageOrdersEventData)).thenReturn("title");

        String actual = underTest.generate(CaseData.builder()
            .manageOrdersEventData(manageOrdersEventData)
            .build());

        assertThat(actual).isEqualTo("title (C43)");
    }

    @Test
    void testIfAnyOtherOrderType() {
        Order order = Order.C21_BLANK_ORDER;

        String actual = underTest.generate(CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(order)
                .build())
            .build());

        assertThat(actual).isEqualTo(order.getHistoryTitle());
    }
}
