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

    @Test
    void shouldSetAnEmptyStringWhenTheValueIsLessThan1() {
        String result = ChildSelector.setChildCountFromInt(0);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldSetAStringWithNumbersInAscendingOrderWhenTheValueIsGreaterThan0() {
        String result = ChildSelector.setChildCountFromInt(6);
        assertThat(result).isEqualTo("123456");
    }

    @Test
    void shouldGenerateHiddenListWhenFinalOrderIssuedOnSomeChildren() {
        List<Element<Child>> children = List.of(buildChild(false), buildChild(true),
            buildChild(true));
        List<Integer> result = ChildSelector.generatedHiddenList(children);

        assertThat(result).containsExactly(1,2);
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
