package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.io.IOException;
import java.util.List;

@JsonComponent
public class DynamicListDeserializer extends JsonDeserializer<DynamicList> {

    @Override
    public DynamicList deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        final ObjectCodec codec = p.getCodec();
        final JsonNode node = codec.readTree(p);

        if (node.isNull()) {
            return null;
        }

        if (node.isValueNode()) {
            final DynamicListElement selected = DynamicListElement.builder()
                .code(node.textValue())
                .label(node.textValue())
                .build();

            return DynamicList.builder()
                .value(selected)
                .listItems(List.of(selected))
                .build();
        }

        return codec.readValue(node.traverse(), DynamicList.class);
    }
}
