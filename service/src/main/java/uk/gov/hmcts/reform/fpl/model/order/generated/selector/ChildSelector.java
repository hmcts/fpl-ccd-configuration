package uk.gov.hmcts.reform.fpl.model.order.generated.selector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ChildSelector {
    private String childCountContainer;
    private Boolean child1;
    private Boolean child2;
    private Boolean child3;
    private Boolean child4;
    private Boolean child5;
    private Boolean child6;
    private Boolean child7;
    private Boolean child8;
    private Boolean child9;
    private Boolean child10;

    // TODO: 31/01/2020 test me
    public void populateChildCountContainer(int max) {
        if (max < 1) {
            childCountContainer = "";
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= max; i++) {
            builder.append(i);
        }

        setChildCountContainer(builder.toString());
    }

    // TODO: 31/01/2020 test me
    @JsonIgnore
    public List<Integer> getSelected() {
        List<Integer> selected = new ArrayList<>();
        addSelectedChild(selected, child1, 0);
        addSelectedChild(selected, child2, 1);
        addSelectedChild(selected, child3, 2);
        addSelectedChild(selected, child4, 3);
        addSelectedChild(selected, child5, 4);
        addSelectedChild(selected, child6, 5);
        addSelectedChild(selected, child7, 6);
        addSelectedChild(selected, child8, 7);
        addSelectedChild(selected, child9, 8);
        addSelectedChild(selected, child10, 9);
        return selected;
    }

    private void addSelectedChild(List<Integer> selected, boolean value, int child) {
        if (value) {
            selected.add(child);
        }
    }
}
