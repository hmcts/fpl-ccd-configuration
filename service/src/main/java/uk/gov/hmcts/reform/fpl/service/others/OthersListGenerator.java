package uk.gov.hmcts.reform.fpl.service.others;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OthersListGenerator {

    private final DynamicListService dynamicLists;

    public DynamicList buildOthersList(List<Element<Other>> others) {
        return dynamicLists.asDynamicList(
            others,
            (e) -> String.valueOf(e.getId()),
            (e) -> e.getValue().getName());
    }

}
