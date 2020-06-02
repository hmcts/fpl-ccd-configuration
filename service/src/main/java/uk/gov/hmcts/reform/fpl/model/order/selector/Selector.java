package uk.gov.hmcts.reform.fpl.model.order.selector;

import java.util.List;

public interface Selector {

    default Selector setCount(int max) {
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= max; i++) {
            builder.append(i);
        }

        setCount(builder.toString());
        return this;
    }

    void setCount(String count);

    String getCount();

    List<Integer> getSelected();

    void setSelected(List<Integer> selected);

    List<Integer> getHidden();

    void setHidden(List<Integer> hidden);
}
