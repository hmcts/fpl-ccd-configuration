package uk.gov.hmcts.reform.fpl.service.others;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.SelectableOther;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;
import uk.gov.hmcts.reform.fpl.utils.IncrementalInteger;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OthersListGenerator {

    private final DynamicListService dynamicLists;

    public DynamicList buildOthersList(List<Element<Other>> others) {
        List<SelectableOther> selectableOthers = new ArrayList<>();
        selectableOthers.addAll(buildOthersElements(others)); // Others to give notice
        return dynamicLists.asDynamicList(
            selectableOthers,
            SelectableOther::getCode,
            SelectableOther::getName);
    }

    private List<SelectableOther> buildOthersElements(List<Element<Other>> others) {
        IncrementalInteger i = new IncrementalInteger(1);
        List<SelectableOther> ret = new ArrayList<>();
        others.forEach(other -> ret.add(
            SelectableOther.builder()
                .code(String.valueOf(other.getId()))
                .name(other.getValue().getName())
                .build())
        );
        return ret;
    }

}
