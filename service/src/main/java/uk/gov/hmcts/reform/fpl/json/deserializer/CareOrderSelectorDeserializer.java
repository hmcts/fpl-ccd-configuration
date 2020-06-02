package uk.gov.hmcts.reform.fpl.json.deserializer;

import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.model.order.selector.CareOrderSelector;

@JsonComponent
public class CareOrderSelectorDeserializer extends SelectorDeserializer<CareOrderSelector> {

    public CareOrderSelectorDeserializer() {
        super("careOrder");
    }

    @Override
    CareOrderSelector newSelector() {
        return CareOrderSelector.builder().build();
    }
}
