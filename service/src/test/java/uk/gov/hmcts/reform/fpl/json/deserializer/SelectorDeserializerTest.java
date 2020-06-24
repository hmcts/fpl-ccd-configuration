package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SelectorDeserializerTest extends DeserializerTest {

    SelectorDeserializerTest() {
        super(Selector.class, new SelectorDeserializer());
    }

    @Test
    void shouldCreateSelectorWhenArraysAreEmpty() throws JsonProcessingException {
        String jsonString = "{"
            + "\"optionCount\":\"\","
            + "\"option1\":[],\"option2\":[],"
            + "\"option3\":[],\"option4\":[],"
            + "\"option5\":[],\"option6\":[],"
            + "\"option7\":[],\"option8\":[],"
            + "\"option9\":[],\"option10\":[]"
            + "}";
        Selector actual = mapper.readValue(jsonString, Selector.class);
        Selector expected = Selector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateSelectorWhenNoArraysPresent() throws JsonProcessingException {
        String jsonString = "{\"optionCount\":\"\"}";
        Selector actual = mapper.readValue(jsonString, Selector.class);
        Selector expected = Selector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateSelectorWithTrueValuesWhenArraysAreOnlyPopulatedWithSelected()
        throws JsonProcessingException {
        String jsonString = "{"
            + "\"optionCount\":\"\","
            + "\"option1\":[\"SELECTED\"],\"option2\":[],"
            + "\"option3\":[],\"option4\":[],"
            + "\"option5\":[],\"option6\":[],"
            + "\"option7\":[\"SELECTED\"],\"option8\":[],"
            + "\"option9\":[\"P1\"],\"option10\":[\"P1\", \"SELECTED\"]"
            + "}";
        Selector actual = mapper.readValue(jsonString, Selector.class);
        Selector expected = Selector.builder().selected(List.of(1, 7)).build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateSelectorWhenCountContainerIsNull() throws JsonProcessingException {
        String jsonString = "{"
            + "\"optionCount\":null,"
            + "\"option1\":[],\"option2\":[],"
            + "\"option3\":[],\"option4\":[],"
            + "\"option5\":[],\"option6\":[],"
            + "\"option7\":[],\"option8\":[],"
            + "\"option9\":[],\"option10\":[]"
            + "}";
        Selector actual = mapper.readValue(jsonString, Selector.class);
        Selector expected = Selector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateSelectorWhenArraysAreNull() throws JsonProcessingException {
        String jsonString = "{"
            + "\"optionCount\":null,"
            + "\"option1\":[],\"option2\":null,"
            + "\"option3\":[],\"option4\":[],"
            + "\"option5\":[],\"option6\":null,"
            + "\"option7\":[],\"option8\":[],"
            + "\"option9\":[],\"option10\":[]"
            + "}";
        Selector actual = mapper.readValue(jsonString, Selector.class);
        Selector expected = Selector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldCreateDefaultSelectorWhenJsonObjectIsEmpty() throws JsonProcessingException {
        String jsonString = "{}";
        Selector actual = mapper.readValue(jsonString, Selector.class);
        Selector expected = Selector.builder().build();
        assertThat(actual).isEqualTo(expected);
    }
}


