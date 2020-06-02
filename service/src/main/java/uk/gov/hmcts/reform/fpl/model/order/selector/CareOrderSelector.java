package uk.gov.hmcts.reform.fpl.model.order.selector;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CareOrderSelector implements Selector {
    @Builder.Default
    protected List<Integer> selected = new ArrayList<>();

    @Builder.Default
    protected List<Integer> hidden = new ArrayList<>();

    @Builder.Default
    private String careOrderCount = "";

    @Override
    public void setCount(String count) {
        this.careOrderCount = count;
    }

    @Override
    public String getCount() {
        return careOrderCount;
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

    public static CareOrderSelector newCareOrderSelector(int size) {
        CareOrderSelector careOrderSelector = CareOrderSelector.builder().build();
        careOrderSelector.setCount(size);
        return careOrderSelector;
    }

}
