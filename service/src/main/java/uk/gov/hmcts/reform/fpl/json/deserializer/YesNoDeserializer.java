package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.jackson.JsonComponent;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@JsonComponent
public class YesNoDeserializer extends JsonDeserializer<YesNo> {

    @Override
    public YesNo deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String enumAsString = parser.getText();
        return isNotEmpty(enumAsString) ? YesNo.valueOf(enumAsString) : null;
    }
}
