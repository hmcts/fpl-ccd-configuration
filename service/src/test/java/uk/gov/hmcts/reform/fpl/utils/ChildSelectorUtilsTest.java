package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.order.generated.selector.ChildSelector;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ChildSelectorUtils.getSelectedIndexes;
import static uk.gov.hmcts.reform.fpl.utils.ChildSelectorUtils.populateChildCountContainer;

class ChildSelectorUtilsTest {

    @Test
    void shouldReturnAnEmptyListWhenNoChildrenAreTrue() {
        assertThat(getSelectedIndexes(ChildSelector.builder().build())).isEmpty();
    }

    @Test
    void shouldReturnAListWithRelativeIndexesWhenSomeChildrenAreTrue() {
        ChildSelector childSelector = ChildSelector.builder().child1(true).child4(true).child9(true).build();
        List<Integer> expected = List.of(0,3,8);
        assertThat(getSelectedIndexes(childSelector)).isEqualTo(expected);
    }

    @Test
    void shouldSetAnEmptyStringWhenTheValueIsLessThan1() {
        ChildSelector childSelector = ChildSelector.builder().build();
        populateChildCountContainer(childSelector, 0);
        assertThat(childSelector.getChildCountContainer()).isEmpty();
    }

    @Test
    void shouldSetAStringWithNumbersInAscendingOrderWhenTheValueIsGreaterThan0() {
        ChildSelector childSelector = ChildSelector.builder().build();
        populateChildCountContainer(childSelector, 6);
        assertThat(childSelector.getChildCountContainer()).isEqualTo("123456");
    }
}
