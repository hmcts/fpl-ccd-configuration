package uk.gov.hmcts.reform.fpl.model.order.selector;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

class ChildSelectorTest {

    ChildSelector childSelector = ChildSelector.builder().build();

    @Test
    void shouldSetAnEmptyStringWhenTheValueIsLessThan1() {
        childSelector.setCount(0);
        assertThat(childSelector.getCount()).isEmpty();
    }

    @Test
    void shouldSetAStringWithNumbersInAscendingOrderWhenTheValueIsGreaterThan0() {
        childSelector.setCount(6);
        assertThat(childSelector.getChildCount()).isEqualTo("123456");
    }

    @Test
    void shouldGenerateHiddenListWhenFinalOrderIssuedOnSomeChildren() {
        List<Element<Child>> children = List.of(buildChild(false), buildChild(true),
            buildChild(true));
        childSelector.updateHidden(children);

        assertThat(childSelector.getHidden()).containsExactly(1,2);
    }

    private Element<Child> buildChild(boolean finalOrderIssued) {
        Element<Child> child = testChild();
        if (finalOrderIssued) {
            child.getValue().setFinalOrderIssued(YesNo.YES.getValue());
            child.getValue().setFinalOrderIssuedType(GeneratedOrderType.CARE_ORDER.getLabel());
        }

        return child;
    }
}
