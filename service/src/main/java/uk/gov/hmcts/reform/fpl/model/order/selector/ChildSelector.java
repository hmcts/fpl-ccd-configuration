package uk.gov.hmcts.reform.fpl.model.order.selector;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ChildSelectorType;

import java.util.List;

@Data
@Builder
public class ChildSelector {
    private String childCount;
    private List<ChildSelectorType> child1;
    private List<ChildSelectorType> child2;
    private List<ChildSelectorType> child3;
    private List<ChildSelectorType> child4;
    private List<ChildSelectorType> child5;
    private List<ChildSelectorType> child6;
    private List<ChildSelectorType> child7;
    private List<ChildSelectorType> child8;
    private List<ChildSelectorType> child9;
    private List<ChildSelectorType> child10;
    private List<ChildSelectorType> child11;
    private List<ChildSelectorType> child12;
    private List<ChildSelectorType> child13;
    private List<ChildSelectorType> child14;
    private List<ChildSelectorType> child15;
}
