package uk.gov.hmcts.reform.fpl.json.serializer;

import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;

@JsonComponent
public class ChildSelectorSerializer extends SelectorSerializer<ChildSelector> {

    public ChildSelectorSerializer() {
        super("child");
    }
}
