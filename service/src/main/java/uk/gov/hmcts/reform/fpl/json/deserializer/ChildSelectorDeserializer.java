package uk.gov.hmcts.reform.fpl.json.deserializer;

import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;

@JsonComponent
public class ChildSelectorDeserializer extends SelectorDeserializer<ChildSelector> {

    public ChildSelectorDeserializer() {
        super("child");
    }

    @Override
    ChildSelector newSelector() {
        return ChildSelector.builder().build();
    }
}
