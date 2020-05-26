package uk.gov.hmcts.reform.fpl.model.order.selector;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ChildSelector {
    @Setter(AccessLevel.PRIVATE)
    @Builder.Default
    private String childCount = "";
    @Builder.Default
    private Integer remainingChildCount = 0;
    @Builder.Default
    private List<Integer> selected = new ArrayList<>();
    @Builder.Default
    private List<Integer> hidden = new ArrayList<>();

    public static String generateChildCount(int max) {
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= max; i++) {
            builder.append(i);
        }
        return builder.toString();
    }

    public static List<Integer> generatedHiddenList(List<Element<Child>> allChildren) {
        List<Integer> hiddenList = new ArrayList<>();
        for (int i = 0; i < allChildren.size(); i++) {
            if (YesNo.YES.getValue().equals(allChildren.get(i).getValue().getFinalOrderIssued())) {
                hiddenList.add(i);
            }
        }
        return hiddenList;
    }
}
