package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.order.generated.selector.ChildSelector;

import java.util.ArrayList;
import java.util.List;

public class ChildSelectorUtils {

    private ChildSelectorUtils() {

    }

    public static void populateChildCountContainer(ChildSelector selector, int max) {
        if (max < 1) {
            selector.setChildCountContainer("");
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= max; i++) {
            builder.append(i);
        }

        selector.setChildCountContainer(builder.toString());
    }

    public static List<Integer> getSelectedIndexes(ChildSelector selector) {
        List<Integer> selected = new ArrayList<>();
        addSelectedChild(selected, selector.isChild1(), 0);
        addSelectedChild(selected, selector.isChild2(), 1);
        addSelectedChild(selected, selector.isChild3(), 2);
        addSelectedChild(selected, selector.isChild4(), 3);
        addSelectedChild(selected, selector.isChild5(), 4);
        addSelectedChild(selected, selector.isChild6(), 5);
        addSelectedChild(selected, selector.isChild7(), 6);
        addSelectedChild(selected, selector.isChild8(), 7);
        addSelectedChild(selected, selector.isChild9(), 8);
        addSelectedChild(selected, selector.isChild10(), 9);
        return selected;
    }

    private static void addSelectedChild(List<Integer> selected, boolean value, int child) {
        if (value) {
            selected.add(child);
        }
    }
}
