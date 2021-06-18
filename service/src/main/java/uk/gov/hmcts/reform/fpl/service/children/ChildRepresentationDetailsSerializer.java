package uk.gov.hmcts.reform.fpl.service.children;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.IdentityService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChildRepresentationDetailsSerializer {

    private static final int MAX_CHILDREN = 15;

    private final IdentityService identityService;

    public Map<String, Object> serialise(List<Element<Child>> children) {
        List<Element<Child>> safeCollection = defaultIfNull(children, new ArrayList<>());

        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < MAX_CHILDREN; i++) {
            data.put(String.format("childRepresentationDetails%d", i), transformOrNull(safeCollection, i));
        }

        return data;
    }

    private Element<ChildRepresentationDetails> transformOrNull(List<Element<Child>> safeCollection, int idx) {
        return idx < safeCollection.size() ? transform(safeCollection.get(idx), idx) : null;
    }

    private Element<ChildRepresentationDetails> transform(Element<Child> childElement, int idx) {
        return element(identityService.generateId(), ChildRepresentationDetails.builder()
            .childDescription(String.format("Child %d - %s", idx + 1, childElement.getValue().getParty().getFullName()))
            .build());
    }
}
