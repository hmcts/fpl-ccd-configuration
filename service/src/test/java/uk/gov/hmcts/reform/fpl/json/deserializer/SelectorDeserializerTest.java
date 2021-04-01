package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SelectorDeserializerTest extends DeserializerTest {

    SelectorDeserializerTest() {
        super(Selector.class, new SelectorDeserializer());
    }

    private static Stream<String> createSelectorWithArrays() {
        return Stream.of(
            getSelectorDataWithEmptyArrays(),
            getSelectorDataWithNullCountContainer(),
            getSelectorDataWithNullArrays(),
            "{\"optionCount\":\"\"}", // json with no arrays
            "{}" // empty json
        );
    }

    @ParameterizedTest
    @MethodSource("createSelectorWithArrays")
    void shouldCreateEmptySelector(String jsonString) throws JsonProcessingException {
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

    private static String getSelectorDataWithEmptyArrays() {
        return "{"
            + "\"optionCount\":\"\","
            + "\"option1\":[],\"option2\":[],"
            + "\"option3\":[],\"option4\":[],"
            + "\"option5\":[],\"option6\":[],"
            + "\"option7\":[],\"option8\":[],"
            + "\"option9\":[],\"option10\":[]"
            + "}";
    }

    private static String getSelectorDataWithNullCountContainer() {
        return "{"
            + "\"optionCount\":null,"
            + "\"option1\":[],\"option2\":[],"
            + "\"option3\":[],\"option4\":[],"
            + "\"option5\":[],\"option6\":[],"
            + "\"option7\":[],\"option8\":[],"
            + "\"option9\":[],\"option10\":[]"
            + "}";
    }

    private static String getSelectorDataWithNullArrays() {
        return "{"
            + "\"optionCount\":null,"
            + "\"option1\":[],\"option2\":null,"
            + "\"option3\":[],\"option4\":[],"
            + "\"option5\":[],\"option6\":null,"
            + "\"option7\":[],\"option8\":[],"
            + "\"option9\":[],\"option10\":[]"
            + "}";
    }
}


