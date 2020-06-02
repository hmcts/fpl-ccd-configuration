package uk.gov.hmcts.reform.fpl.model.order.selector;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Data
@Builder
public class ChildSelector implements Selector {
    @Builder.Default
    protected List<Integer> selected = new ArrayList<>();

    @Builder.Default
    protected List<Integer> hidden = new ArrayList<>();

    @Builder.Default
    private String childCount = "";

    @Override
    public void setCount(String count) {
        this.childCount = count;
    }

    @Override
    public String getCount() {
        return childCount;
    }

    @Override
    public List<Integer> getSelected() {
        return selected;
    }

    @Override
    public List<Integer> getHidden() {
        return hidden;
    }

    @Override
    public void setHidden(List<Integer> hidden) {
        this.hidden = new ArrayList<>(hidden);
    }

    @Override
    public void setSelected(List<Integer> selected) {
        this.selected = new ArrayList<>(selected);
    }

    public void updateHidden(List<Element<Child>> children) {
        List<Integer> hiddenList = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            if (YES.getValue().equals(children.get(i).getValue().getFinalOrderIssued())) {
                hiddenList.add(i);
            }
        }
        setHidden(hiddenList);
    }

    public static ChildSelector newChildSelector(List<Element<Child>> children) {
        ChildSelector childSelector = ChildSelector.builder().build();
        childSelector.setCount(children.size());
        return childSelector;
    }

}
