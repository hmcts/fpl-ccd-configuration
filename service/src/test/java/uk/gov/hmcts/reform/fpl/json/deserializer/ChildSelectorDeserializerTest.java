package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.order.generated.selector.ChildSelector;

import static org.assertj.core.api.Assertions.assertThat;

class ChildSelectorDeserializerTest extends DeserializerTest {

    ChildSelectorDeserializerTest() {
        super(ChildSelector.class, new ChildSelectorDeserializer());
    }

    @Test
    void shouldCreateDefaultChildSelectorWhenArraysAreEmpty() throws JsonProcessingException {
        String jsonString = "{" +
            "\"childCountContainer\":\"\"," +
            "\"child1\":[],\"child2\":[]," +
            "\"child3\":[],\"child4\":[]," +
            "\"child5\":[],\"child6\":[]," +
            "\"child7\":[],\"child8\":[]," +
            "\"child9\":[],\"child10\":[]" +
            "}";
        ChildSelector actual = mapper.readValue(jsonString, ChildSelector.class);
        ChildSelector expected = ChildSelector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateChildSelectorWithTrueValuesWhenArraysArePopulated() throws JsonProcessingException {
        String jsonString = "{" +
            "\"childCountContainer\":\"\"," +
            "\"child1\":[\"selected\"],\"child2\":[]," +
            "\"child3\":[],\"child4\":[]," +
            "\"child5\":[],\"child6\":[]," +
            "\"child7\":[\"anything\"],\"child8\":[]," +
            "\"child9\":[],\"child10\":[]" +
            "}";
        ChildSelector actual = mapper.readValue(jsonString, ChildSelector.class);
        ChildSelector expected = ChildSelector.builder().child1(true).child7(true).build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateDefaultChildSelectorWhenChildCountContainerIsNull() throws JsonProcessingException {
        String jsonString = "{" +
            "\"childCountContainer\":null," +
            "\"child1\":[],\"child2\":[]," +
            "\"child3\":[],\"child4\":[]," +
            "\"child5\":[],\"child6\":[]," +
            "\"child7\":[],\"child8\":[]," +
            "\"child9\":[],\"child10\":[]" +
            "}";
        ChildSelector actual = mapper.readValue(jsonString, ChildSelector.class);
        ChildSelector expected = ChildSelector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateDefaultChildSelectorWhenArraysAreNull() throws JsonProcessingException {
        String jsonString = "{" +
            "\"childCountContainer\":null," +
            "\"child1\":[],\"child2\":null," +
            "\"child3\":[],\"child4\":[]," +
            "\"child5\":[],\"child6\":null," +
            "\"child7\":[],\"child8\":[]," +
            "\"child9\":[],\"child10\":[]" +
            "}";
        ChildSelector actual = mapper.readValue(jsonString, ChildSelector.class);
        ChildSelector expected = ChildSelector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateDefaultChildSelectorWhenJsonObjectIsEmpty() throws JsonProcessingException {
        String jsonString = "{}";
        ChildSelector actual = mapper.readValue(jsonString, ChildSelector.class);
        ChildSelector expected = ChildSelector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }
}
