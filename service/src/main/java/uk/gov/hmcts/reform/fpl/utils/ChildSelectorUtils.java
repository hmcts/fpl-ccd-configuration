package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ChildSelectorType;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;

import java.util.ArrayList;
import java.util.List;

public class ChildSelectorUtils {

    private ChildSelectorUtils() {

    }

    public static String generateChildCount(int max) {
        if (max < 1) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= max; i++) {
            builder.append(i);
        }

        return builder.toString();
    }

    public static List<Integer> getSelectedIndexes(ChildSelector selector) {
        List<Integer> selected = new ArrayList<>();
        addSelectedChild(selected, selector.getChild1(), 0);
        addSelectedChild(selected, selector.getChild2(), 1);
        addSelectedChild(selected, selector.getChild3(), 2);
        addSelectedChild(selected, selector.getChild4(), 3);
        addSelectedChild(selected, selector.getChild5(), 4);
        addSelectedChild(selected, selector.getChild6(), 5);
        addSelectedChild(selected, selector.getChild7(), 6);
        addSelectedChild(selected, selector.getChild8(), 7);
        addSelectedChild(selected, selector.getChild9(), 8);
        addSelectedChild(selected, selector.getChild10(), 9);
        return selected;
    }

    private static void addSelectedChild(List<Integer> selected, List<ChildSelectorType> value, int child) {
        if (value != null && !value.isEmpty()) {
            selected.add(child);
        }
    }
}
