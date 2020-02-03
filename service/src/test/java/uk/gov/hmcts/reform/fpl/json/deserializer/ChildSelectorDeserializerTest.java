package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.order.generated.selector.ChildSelector;

import static org.assertj.core.api.Assertions.assertThat;

class ChildSelectorDeserializerTest extends DeserializerTest {
    String jsonString;
    ChildSelector actual;
    ChildSelector expected;

    ChildSelectorDeserializerTest() {
        super(ChildSelector.class, new ChildSelectorDeserializer());
    }

    @Test
    void shouldCreateDefaultChildSelectorWhenArraysAreEmpty() throws JsonProcessingException {
        jsonString = buildJsonString("\"\"", new String[] {"", "", "", "", "", "", "", "", "", ""});
        actual = mapper.readValue(jsonString, ChildSelector.class);
        expected = ChildSelector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateObjectWithTrueValuesWhenArraysArePopulated() throws JsonProcessingException {
        jsonString = buildJsonString("\"\"",
            new String[] {"\"selected\"", "", "", "", "", "", "\"anything\"", "", "", ""});
        actual = mapper.readValue(jsonString, ChildSelector.class);
        expected = ChildSelector.builder().child1(true).child7(true).build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateDefaultChildSelectorWhenChildCountContainerIsNull() throws JsonProcessingException {
        jsonString = buildJsonString(null, new String[] {"", "", "", "", "", "", "", "", "", ""});
        actual = mapper.readValue(jsonString, ChildSelector.class);
        expected = ChildSelector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateDefaultChildSelectorWhenArraysAreNull() throws JsonProcessingException {
        jsonString = buildJsonString(null, new String[] {"", null, "", "", "", null, "", "", "", ""});
        actual = mapper.readValue(jsonString, ChildSelector.class);
        expected = ChildSelector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateDefaultChildSelectorWhenJsonObjectIsEmpty() throws JsonProcessingException {
        jsonString = "{}";
        actual = mapper.readValue(jsonString, ChildSelector.class);
        expected = ChildSelector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    private String buildJsonString(String childCount, String[] childValues) {
        StringBuilder builder = new StringBuilder("{");
        builder.append(wrapField("childCountContainer")).append(childCount);

        for (int i = 0; i < childValues.length; i++) {
            builder.append(",")
                .append(wrapField("child" + (i + 1)))
                .append(wrapAsArray(childValues[i]));
        }

        builder.append("}");
        return builder.toString();
    }

    private String wrapField(String fieldName) {
        return String.format("\"%s\":", fieldName);
    }

    private String wrapAsArray(String value) {
        if (value == null) {
            return null;
        }
        return String.format("[%s]", value);
    }
}
