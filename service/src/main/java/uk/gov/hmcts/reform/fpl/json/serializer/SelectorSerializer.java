package uk.gov.hmcts.reform.fpl.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.SelectorType.SELECTED;

@JsonComponent
public class SelectorSerializer extends JsonSerializer<Selector> {

    private static final String OPTION_BASE_NAME = "option";
    private static final String OPTIONS_COUNT_NAME = "optionCount";

    @Override
    public void serialize(Selector value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField(OPTIONS_COUNT_NAME, defaultIfNull(value.getCount(), ""));

        List<Integer> selected = defaultIfNull(value.getSelected(), new ArrayList<>());
        int max = selected.stream().mapToInt(i -> i + 1).max().orElse(0);

        for (int i = 0; i < max; i++) {
            generateOption(gen, i, selected.contains(i));
        }

        List<Integer> hidden = defaultIfNull(value.getHidden(), new ArrayList<>());
        max = hidden.stream().mapToInt(i -> i + 1).max().orElse(0);

        for (int i = 0; i < max; i++) {
            gen.writeObjectField(OPTION_BASE_NAME + i + "Hidden", hidden.contains(i) ? "Yes" : "No");
        }

        gen.writeEndObject();
    }

    private void generateOption(JsonGenerator generator, int i, boolean value) throws IOException {
        generator.writeObjectField(OPTION_BASE_NAME + i, toArray(value));
    }

    private String[] toArray(boolean selected) {
        return selected ? new String[]{SELECTED.name()} : new String[]{};
    }
}
