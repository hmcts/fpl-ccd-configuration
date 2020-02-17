package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChildSelectorDeserializerTest extends DeserializerTest {

    ChildSelectorDeserializerTest() {
        super(ChildSelector.class, new ChildSelectorDeserializer());
    }

    @Test
    void shouldCreateDefaultChildSelectorWhenArraysAreEmpty() throws JsonProcessingException {
        String jsonString = "{"
            + "\"childCount\":\"\","
            + "\"child1\":[],\"child2\":[],"
            + "\"child3\":[],\"child4\":[],"
            + "\"child5\":[],\"child6\":[],"
            + "\"child7\":[],\"child8\":[],"
            + "\"child9\":[],\"child10\":[]"
            + "}";
        ChildSelector actual = mapper.readValue(jsonString, ChildSelector.class);
        ChildSelector expected = ChildSelector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateDefaultChildSelectorWhenNoArraysPresent() throws JsonProcessingException {
        String jsonString = "{\"childCount\":\"\"}";
        ChildSelector actual = mapper.readValue(jsonString, ChildSelector.class);
        ChildSelector expected = ChildSelector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateChildSelectorWithTrueValuesWhenArraysArePopulated() throws JsonProcessingException {
        String jsonString = "{"
            + "\"childCount\":\"\","
            + "\"child1\":[\"SELECTED\"],\"child2\":[],"
            + "\"child3\":[],\"child4\":[],"
            + "\"child5\":[],\"child6\":[],"
            + "\"child7\":[\"SELECTED\"],\"child8\":[],"
            + "\"child9\":[],\"child10\":[]"
            + "}";
        ChildSelector actual = mapper.readValue(jsonString, ChildSelector.class);
        ChildSelector expected = ChildSelector.builder().selected(List.of(1, 7)).build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateDefaultChildSelectorWhenChildCountContainerIsNull() throws JsonProcessingException {
        String jsonString = "{"
            + "\"childCount\":null,"
            + "\"child1\":[],\"child2\":[],"
            + "\"child3\":[],\"child4\":[],"
            + "\"child5\":[],\"child6\":[],"
            + "\"child7\":[],\"child8\":[],"
            + "\"child9\":[],\"child10\":[]"
            + "}";
        ChildSelector actual = mapper.readValue(jsonString, ChildSelector.class);
        ChildSelector expected = ChildSelector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateDefaultChildSelectorWhenArraysAreNull() throws JsonProcessingException {
        String jsonString = "{"
            + "\"childCount\":null,"
            + "\"child1\":[],\"child2\":null,"
            + "\"child3\":[],\"child4\":[],"
            + "\"child5\":[],\"child6\":null,"
            + "\"child7\":[],\"child8\":[],"
            + "\"child9\":[],\"child10\":[]"
            + "}";
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


