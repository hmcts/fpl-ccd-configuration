package uk.gov.hmcts.reform.fpl.model.order.selector;

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
    @Builder.Default
    private String childCount = "";
    @Builder.Default
    private List<Integer> selected = new ArrayList<>();

    public void generateChildCount(int max) {
        if (max < 1) {
            setChildCount("");
        } else {
            StringBuilder builder = new StringBuilder();

            for (int i = 1; i <= max; i++) {
                builder.append(i);
            }

            setChildCount(builder.toString());
        }
    }
}
