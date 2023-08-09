package uk.gov.hmcts.reform.fpl.model.order.selector;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SelectorTest {

    private Selector selector = Selector.builder().build();

    @Test
    void shouldSetAnEmptyStringWhenNumberOfOptionsIsZero() {
        selector.setNumberOfOptions(0);
        assertThat(selector.getCount()).isEmpty();
    }

    @Test
    void shouldSetAnEmptyStringWhenNumberOfOptionsIsNotSpecified() {
        selector.setNumberOfOptions(null);
        assertThat(selector.getCount()).isEmpty();
    }

    @Test
    void shouldSetAStringWithNumbersInAscendingOrderWhenNumberOfOptionsIsPositive() {
        selector.setNumberOfOptions(6);
        assertThat(selector.getCount()).isEqualTo("123456");
    }

    @Test
    void shouldSetAStringWithLowerAndUpperLimitInAscendingOrderWhenNumberOfOptionsIsPositive() {
        selector.setNumberOfOptions(3, 6);
        assertThat(selector.getCount()).isEqualTo("3456");
    }

}
