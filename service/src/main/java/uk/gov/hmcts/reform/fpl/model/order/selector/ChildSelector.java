package uk.gov.hmcts.reform.fpl.model.order.selector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.types.CCD;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Data
@Builder
public class ChildSelector {
    @Setter(AccessLevel.PRIVATE)
    @Builder.Default
    private String childCount = "";
    @Builder.Default
    @CCD(ignore = true)
    private List<Integer> selected = new ArrayList<>();
    @Builder.Default
    private List<Integer> hidden = new ArrayList<>();

    @JsonIgnore
    public void setChildCountFromInt(int max) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= max; i++) {
            builder.append(i);
        }
        setChildCount(builder.toString());
    }

    @JsonIgnore
    public void setHiddenFromChildList(List<Element<Child>> children) {
        List<Integer> hiddenList = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            if (YES.getValue().equals(children.get(i).getValue().getFinalOrderIssued())) {
                hiddenList.add(i);
            }
        }
        setHidden(hiddenList);
    }
}
