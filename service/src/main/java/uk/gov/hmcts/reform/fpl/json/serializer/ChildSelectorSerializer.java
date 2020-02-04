package uk.gov.hmcts.reform.fpl.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.model.order.generated.selector.ChildSelector;

import java.io.IOException;

@JsonComponent
public class ChildSelectorSerializer extends JsonSerializer<ChildSelector> {
    @Override
    public void serialize(ChildSelector value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("childCount", value.getChildCount());
        generateChild(gen, 1, value.isChild1());
        generateChild(gen, 2, value.isChild2());
        generateChild(gen, 3, value.isChild3());
        generateChild(gen, 4, value.isChild4());
        generateChild(gen, 5, value.isChild5());
        generateChild(gen, 6, value.isChild6());
        generateChild(gen, 7, value.isChild7());
        generateChild(gen, 8, value.isChild8());
        generateChild(gen, 9, value.isChild9());
        generateChild(gen, 10, value.isChild10());
        gen.writeEndObject();
    }

    private void generateChild(JsonGenerator generator, int i, boolean value) throws IOException {
        generator.writeObjectField("child" + i, toArray(value));
    }

    public String[] toArray(Boolean selected) {
        return selected != null && selected ? new String[] {"SELECTED"} : new String[]{};
    }
}
