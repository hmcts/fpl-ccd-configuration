package uk.gov.hmcts.reform.fpl.model.order.selector;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ChildSelectorType;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ChildSelector {
    @Builder.Default
    private String childCount = "";
    @Builder.Default
    private List<ChildSelectorType> child1 = new ArrayList<>();
    @Builder.Default
    private List<ChildSelectorType> child2 = new ArrayList<>();
    @Builder.Default
    private List<ChildSelectorType> child3 = new ArrayList<>();
    @Builder.Default
    private List<ChildSelectorType> child4 = new ArrayList<>();
    @Builder.Default
    private List<ChildSelectorType> child5 = new ArrayList<>();
    @Builder.Default
    private List<ChildSelectorType> child6 = new ArrayList<>();
    @Builder.Default
    private List<ChildSelectorType> child7 = new ArrayList<>();
    @Builder.Default
    private List<ChildSelectorType> child8 = new ArrayList<>();
    @Builder.Default
    private List<ChildSelectorType> child9 = new ArrayList<>();
    @Builder.Default
    private List<ChildSelectorType> child10 = new ArrayList<>();
}
