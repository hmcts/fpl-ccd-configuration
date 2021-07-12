package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChildrenTestHelper {

    private ChildrenTestHelper() {
    }

    public static List<Pair<UUID, String>> buildPairsFromChildrenList(List<Element<Child>> children) {
        return children.stream()
            .map(childElement -> Pair.of(childElement.getId(), childElement.getValue().getParty().getFullName()))
            .collect(Collectors.toList());
    }

}
