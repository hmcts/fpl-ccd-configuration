package uk.gov.hmcts.reform.fpl.json.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.order.selector.ChildSelector;

import java.util.List;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class ChildSelectorSerializerTest extends SerializerTest {

    ChildSelectorSerializerTest() {
        super(ChildSelector.class, new ChildSelectorSerializer());
    }

    @Test
    void shouldOnlyCreateChildCountWhenThereAreNoSelectedValues() throws JsonProcessingException {
        ChildSelector childSelector = ChildSelector.builder().build();
        String actual = mapper.writeValueAsString(childSelector);
        String expected = "{\"childCount\":\"\"}";
        assertEquals(expected, actual, true);
    }

    @Test
    void shouldCreateStringWithPopulatedArraysWhenThereAreSelectedValues() throws JsonProcessingException {
        ChildSelector childSelector = ChildSelector.builder().selected(List.of(0, 4, 9)).build();
        String actual = mapper.writeValueAsString(childSelector);
        String expected = "{"
            + "\"childCount\":\"\","
            + "\"child0\":[\"SELECTED\"],\"child1\":[],"
            + "\"child2\":[],\"child3\":[],"
            + "\"child4\":[\"SELECTED\"],\"child5\":[],"
            + "\"child6\":[],\"child7\":[],"
            + "\"child8\":[],\"child9\":[\"SELECTED\"]"
            + "}";
        assertEquals(expected, actual, true);
    }

    @Test
    void shouldCreateAStringWithNullAssignedToChildCountWhenValueIsNull() throws JsonProcessingException {
        ChildSelector childSelector = ChildSelector.builder().childCount(null).build();
        String actual = mapper.writeValueAsString(childSelector);
        String expected = "{\"childCount\":\"\"}";
        assertEquals(expected, actual, true);
    }

    @Test
    void shouldCreateStringWithPopulatedArraysWhenThereAreHiddenValues() throws JsonProcessingException {
        ChildSelector childSelector = ChildSelector.builder().hidden(List.of(2, 5, 7)).build();
        String actual = mapper.writeValueAsString(childSelector);
        String expected = "{"
            + "\"childCount\":\"\","
            + "\"child0Hidden\":\"No\",\"child1Hidden\":\"No\","
            + "\"child2Hidden\":\"Yes\",\"child3Hidden\":\"No\","
            + "\"child4Hidden\":\"No\",\"child5Hidden\":\"Yes\","
            + "\"child6Hidden\":\"No\",\"child7Hidden\":\"Yes\"}";
        assertEquals(expected, actual, true);
    }
}
