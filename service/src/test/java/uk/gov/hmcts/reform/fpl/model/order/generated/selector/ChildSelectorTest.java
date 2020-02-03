package uk.gov.hmcts.reform.fpl.model.order.generated.selector;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChildSelectorTest {

    private ChildSelector childSelector;

    @Test
    void shouldReturnAnEmptyListWhenNoChildrenAreTrue() {
        childSelector = ChildSelector.builder().build();
        assertThat(childSelector.getSelected()).isEmpty();
    }

    @Test
    void shouldReturnAListWithRelativeIndexesWhenSomeChildrenAreTrue() {
        childSelector = ChildSelector.builder().child1(true).child4(true).child9(true).build();
        List<Integer> expected = List.of(0,3,8);
        assertThat(childSelector.getSelected()).isEqualTo(expected);
    }

    @Test
    void shouldSetAnEmptyStringWhenTheValueIsLessThan1() {
        childSelector = ChildSelector.builder().build();
        childSelector.populateChildCountContainer(0);
        assertThat(childSelector.getChildCountContainer()).isEmpty();
    }

    @Test
    void shouldSetAStringWithNumbersInAscendingOrderWhenTheValueIsGreaterThan0() {
        childSelector = ChildSelector.builder().build();
        childSelector.populateChildCountContainer(6);
        assertThat(childSelector.getChildCountContainer()).isEqualTo("123456");
    }
}
