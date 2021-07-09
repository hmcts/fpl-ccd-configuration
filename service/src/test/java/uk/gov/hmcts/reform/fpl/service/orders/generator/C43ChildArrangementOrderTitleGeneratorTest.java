package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.enums.C43OrderType;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.CHILD_ARRANGEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.PROHIBITED_STEPS_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C43OrderType.SPECIFIC_ISSUE_ORDER;

class C43ChildArrangementOrderTitleGeneratorTest {


    private final C43ChildArrangementOrderTitleGenerator underTest = new C43ChildArrangementOrderTitleGenerator();

    @ParameterizedTest
    @MethodSource("c43OrdersSource")
    void expectedOrderTitleWhenOneOrderSelected(List<C43OrderType> orders, String expectedString) {

        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersMultiSelectListForC43(orders)
            .build();

        String result = underTest.getOrderTitle(manageOrdersEventData);

        assertThat(result).isEqualTo(expectedString);
    }

    private static Stream<Arguments> c43OrdersSource() {
        return Stream.of(
            Arguments.of(List.of(CHILD_ARRANGEMENT_ORDER), "Child arrangements order"),
            Arguments.of(List.of(SPECIFIC_ISSUE_ORDER), "Specific issue order"),
            Arguments.of(List.of(PROHIBITED_STEPS_ORDER), "Prohibited steps order"),
            Arguments.of(List.of(CHILD_ARRANGEMENT_ORDER, SPECIFIC_ISSUE_ORDER),
                "Child arrangements and Specific issue order"),
            Arguments.of(List.of(CHILD_ARRANGEMENT_ORDER, PROHIBITED_STEPS_ORDER),
                "Child arrangements and Prohibited steps order"),
            Arguments.of(List.of(SPECIFIC_ISSUE_ORDER, PROHIBITED_STEPS_ORDER),
                "Specific issue and Prohibited steps order"),
            Arguments.of(List.of(CHILD_ARRANGEMENT_ORDER, SPECIFIC_ISSUE_ORDER, PROHIBITED_STEPS_ORDER),
                "Child arrangements, Specific issue and Prohibited steps order")
        );
    }
}
