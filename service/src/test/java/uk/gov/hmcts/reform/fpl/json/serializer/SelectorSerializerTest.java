package uk.gov.hmcts.reform.fpl.json.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.util.List;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class SelectorSerializerTest extends SerializerTest {

    SelectorSerializerTest() {
        super(Selector.class, new SelectorSerializer());
    }

    @Test
    void shouldCreateEmptyCountWhenNumberOfOptionsIsZero() throws JsonProcessingException {
        Selector selector = Selector.newSelector(0);

        String actual = mapper.writeValueAsString(selector);
        String expected = "{\"optionCount\":\"\"}";
        assertEquals(expected, actual, true);
    }

    @Test
    void shouldCreateEmptyCountWhenNumberOfOptionsIsNotSpecified() throws JsonProcessingException {
        Selector selector = Selector.newSelector(null);
        String actual = mapper.writeValueAsString(selector);
        String expected = "{\"optionCount\":\"\"}";
        assertEquals(expected, actual, true);
    }

    @Test
    void shouldCreateCountWhenNumberOfOptionsIsPositive() throws JsonProcessingException {
        Selector selector = Selector.newSelector(5);

        String actual = mapper.writeValueAsString(selector);
        String expected = "{\"optionCount\":\"12345\"}";
        assertEquals(expected, actual, true);
    }

    @Test
    void shouldCreateStringWithPopulatedArraysWhenThereAreSelectedValues() throws JsonProcessingException {
        Selector optionSelector = Selector.newSelector(10);
        optionSelector.setSelected(List.of(0, 4, 9));

        String actual = mapper.writeValueAsString(optionSelector);
        String expected = "{"
            + "\"optionCount\":\"12345678910\","
            + "\"option0\":[\"SELECTED\"],\"option1\":[],"
            + "\"option2\":[],\"option3\":[],"
            + "\"option4\":[\"SELECTED\"],\"option5\":[],"
            + "\"option6\":[],\"option7\":[],"
            + "\"option8\":[],\"option9\":[\"SELECTED\"]"
            + "}";
        assertEquals(expected, actual, true);
    }
}
