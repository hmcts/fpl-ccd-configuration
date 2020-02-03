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
        childCountContainer = builder.toString();
    }

    // TODO: 31/01/2020 test me
    @JsonIgnore
    public List<Integer> getSelected() {
        List<Integer> selected = new ArrayList<>();
        addSelected(selected, child1, 1);
        addSelected(selected, child2, 2);
        addSelected(selected, child3, 3);
        addSelected(selected, child4, 4);
        addSelected(selected, child5, 5);
        addSelected(selected, child6, 6);
        addSelected(selected, child7, 7);
        addSelected(selected, child8, 8);
        addSelected(selected, child9, 9);
        addSelected(selected, child10, 10);
        return selected;
    }

    private void addSelected(List<Integer> selected, boolean value, int child) {
        if (value) {
            selected.add(child);
        }
    }
}
