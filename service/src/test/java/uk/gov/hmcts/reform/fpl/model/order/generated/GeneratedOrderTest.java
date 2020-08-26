package uk.gov.hmcts.reform.fpl.model.order.generated;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;

class GeneratedOrderTest {

    @Test
    void shouldMarkBlankOrdersAsRemovable() {
        GeneratedOrder order = GeneratedOrder.builder()
            .type(BLANK_ORDER.getLabel())
            .build();

        assertThat(order.isRemovable()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Final care order",
        "Interim care order",
        "Final supervision order",
        "Interim supervision order",
        "Emergency protection order"
    })
    void shouldNotMarkOtherOrdersAreRemovable(String type) {
        GeneratedOrder order = GeneratedOrder.builder()
            .type(type)
            .build();

        assertThat(order.isRemovable()).isFalse();
    }

    @Test
    void shouldReturnTitleAppendedByDateOfIssueWhenTitlePresent() {
        GeneratedOrder order = GeneratedOrder.builder()
            .title("do you remember")
            .dateOfIssue("21st September")
            .build();

        assertThat(order.asLabel()).isEqualTo("do you remember - 21st September");
    }

    @Test
    void shouldReturnTypeAppendedByDateOfIssueWhenTitleNotPresent() {
        GeneratedOrder order = GeneratedOrder.builder()
            .type("dancing in september")
            .dateOfIssue("21 September 1978")
            .build();

        assertThat(order.asLabel()).isEqualTo("dancing in september - 21 September 1978");
    }
}
