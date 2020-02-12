package uk.gov.hmcts.reform.fpl.model.order.selector;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChildSelectorTest {

    ChildSelector childSelector = ChildSelector.builder().build();

    @Test
    void shouldSetAnEmptyStringWhenTheValueIsLessThan1() {
        childSelector.generateChildCount(0);
        assertThat(childSelector.getChildCount()).isEmpty();
    }

    @Test
    void shouldSetAStringWithNumbersInAscendingOrderWhenTheValueIsGreaterThan0() {
        childSelector.generateChildCount(6);
        assertThat(childSelector.getChildCount()).isEqualTo("123456");
    }

}
