package uk.gov.hmcts.reform.fpl.service.orders.history;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.enums.C43OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.CHILD_ARRANGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.PROHIBITED_STEPS_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.SPECIFIC_ISSUE_ORDER;

public class SealedOrderHistoryTypeGeneratorTest {

    private final SealedOrderHistoryTypeGenerator underTest = new SealedOrderHistoryTypeGenerator();

    @ParameterizedTest
    @MethodSource("c43OrdersSource")
    void testIfChildArrangementSpecificIssueProhibitedStepsOrder(List<C43OrderType> orders, String expectedString) {
        String actual = underTest.generate(CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.C43_CHILD_ARRANGEMENT_SPECIFIC_ISSUE_PROHIBITED_STEPS_ORDER)
                .manageOrdersMultiSelectListForC43(orders)
                .build())
            .build());

        assertThat(actual).isEqualTo(expectedString);
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

    private static Stream<Arguments> c43OrdersSource() {
        return Stream.of(
            Arguments.of(List.of(CHILD_ARRANGEMENT_ORDER), "Child arrangements order (C43)"),
            Arguments.of(List.of(SPECIFIC_ISSUE_ORDER), "Specific issue order (C43)"),
            Arguments.of(List.of(PROHIBITED_STEPS_ORDER), "Prohibited steps order (C43)"),
            Arguments.of(List.of(CHILD_ARRANGEMENT_ORDER, SPECIFIC_ISSUE_ORDER),
                "Child arrangements and Specific issue order (C43)"),
            Arguments.of(List.of(CHILD_ARRANGEMENT_ORDER, PROHIBITED_STEPS_ORDER),
                "Child arrangements and Prohibited steps order (C43)"),
            Arguments.of(List.of(SPECIFIC_ISSUE_ORDER, PROHIBITED_STEPS_ORDER),
                "Specific issue and Prohibited steps order (C43)"),
            Arguments.of(List.of(CHILD_ARRANGEMENT_ORDER, SPECIFIC_ISSUE_ORDER, PROHIBITED_STEPS_ORDER),
                "Child arrangements, Specific issue and Prohibited steps order (C43)")
        );
    }
}
