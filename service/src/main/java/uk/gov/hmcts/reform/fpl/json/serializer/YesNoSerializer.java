package uk.gov.hmcts.reform.fpl.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

import java.io.IOException;

public class YesNoSerializer extends JsonSerializer<YesNo> {

    @Override
    public void serialize(YesNo value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value != null ? value.getValue() : null);
    }
}
