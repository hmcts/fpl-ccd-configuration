package uk.gov.hmcts.reform.fpl.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.model.order.generated.selector.ChildSelector;

import java.io.IOException;
import java.util.List;

// TODO: 31/01/2020 Test me
@JsonComponent
public class ChildSelectorSerializer extends JsonSerializer<ChildSelector> {
    @Override
    public void serialize(ChildSelector value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("childCountContainer", value.getChildCountContainer());
        generateChild(gen, 1, value.getChild1());
        generateChild(gen, 2, value.getChild2());
        generateChild(gen, 3, value.getChild3());
        generateChild(gen, 4, value.getChild4());
        generateChild(gen, 5, value.getChild5());
        generateChild(gen, 6, value.getChild6());
        generateChild(gen, 7, value.getChild7());
        generateChild(gen, 8, value.getChild8());
        generateChild(gen, 9, value.getChild9());
        generateChild(gen, 10, value.getChild10());
        gen.writeEndObject();
    }

    private void generateChild(JsonGenerator generator, int i, Boolean value) throws IOException {
        generator.writeObjectField("child" + i, toList(value));
    }

    public List<String> toList(Boolean selected) {
        return selected != null && selected ? List.of("selected") : List.of();
    }
}
