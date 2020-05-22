package uk.gov.hmcts.reform.fpl.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ChildSelectorType.SELECTED;

@JsonComponent
public class ChildSelectorSerializer extends JsonSerializer<ChildSelector> {


    @Override
    public void serialize(ChildSelector value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("childCount", defaultIfNull(value.getChildCount(), ""));

        List<Integer> selected = defaultIfNull(value.getSelected(), new ArrayList<>());
        int max = selected.stream().mapToInt(i -> i + 1).max().orElse(0);

        for (int i = 0; i < max; i++) {
            generateChild(gen, i, selected.contains(i));
        }

        List<Integer> hidden = defaultIfNull(value.getHidden(), new ArrayList<>());
        max = hidden.stream().mapToInt(i -> i + 1).max().orElse(0);

        for (int i = 0; i < max; i++) {
            gen.writeObjectField("child" + i +  "Hidden", hidden.contains(i) ? "Yes" : "No");
        }

        gen.writeEndObject();
    }

    private void generateChild(JsonGenerator generator, int i, boolean value) throws IOException {
        generator.writeObjectField("child" + i, toArray(value));
    }

    private String[] toArray(boolean selected) {
        return selected ? new String[]{SELECTED.name()} : new String[]{};
    }
}
