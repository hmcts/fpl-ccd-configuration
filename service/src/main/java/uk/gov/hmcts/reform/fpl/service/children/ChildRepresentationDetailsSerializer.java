package uk.gov.hmcts.reform.fpl.service.children;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;

@Component
public class ChildRepresentationDetailsSerializer {

    public Map<String,Object> serialise(List<Element<Child>> children) {
        return null;
    }
}
