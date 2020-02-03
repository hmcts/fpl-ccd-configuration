package uk.gov.hmcts.reform.fpl.model.order.generated.selector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ChildSelector {
    @Setter(AccessLevel.PRIVATE)
    private String childCountContainer;
    @Builder.Default
    private boolean child1 = false;
    @Builder.Default
    private boolean child2 = false;
    @Builder.Default
    private boolean child3 = false;
    @Builder.Default
    private boolean child4 = false;
    @Builder.Default
    private boolean child5 = false;
    @Builder.Default
    private boolean child6 = false;
    @Builder.Default
    private boolean child7 = false;
    @Builder.Default
    private boolean child8 = false;
    @Builder.Default
    private boolean child9 = false;
    @Builder.Default
    private boolean child10 = false;

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
