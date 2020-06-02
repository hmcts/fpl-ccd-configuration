package uk.gov.hmcts.reform.fpl.json.serializer;

import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.model.order.selector.CareOrderSelector;

@JsonComponent
public class CareOrderSelectorSerializer extends SelectorSerializer<CareOrderSelector> {

    public CareOrderSelectorSerializer() {
        super("careOrder");
    }
}
