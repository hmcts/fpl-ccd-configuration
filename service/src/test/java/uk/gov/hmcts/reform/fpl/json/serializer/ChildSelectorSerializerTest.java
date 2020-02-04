package uk.gov.hmcts.reform.fpl.json.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.order.generated.selector.ChildSelector;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class ChildSelectorSerializerTest extends SerializerTest {

    protected ChildSelectorSerializerTest() {
        super(ChildSelector.class, new ChildSelectorSerializer());
    }

    @Test
    void shouldCreateStringWithEmptyArraysWhenValuesAreFalse() throws JsonProcessingException {
        ChildSelector childSelector = ChildSelector.builder().build();
        String actual = mapper.writeValueAsString(childSelector);
        String expected = "{"
            + "\"childCountContainer\":\"\","
            + "\"child1\":[],\"child2\":[],"
            + "\"child3\":[],\"child4\":[],"
            + "\"child5\":[],\"child6\":[],"
            + "\"child7\":[],\"child8\":[],"
            + "\"child9\":[],\"child10\":[]"
            + "}";
        assertEquals(expected, actual, true);
    }

    @Test
    void shouldCreateStringWithPopulatedArraysWhenValuesAreTrue() throws JsonProcessingException {
        ChildSelector childSelector = ChildSelector.builder().child1(true).child5(true).child10(true).build();
        String actual = mapper.writeValueAsString(childSelector);
        String expected = "{"
            + "\"childCountContainer\":\"\","
            + "\"child1\":[\"SELECTED\"],\"child2\":[],"
            + "\"child3\":[],\"child4\":[],"
            + "\"child5\":[\"SELECTED\"],\"child6\":[],"
            + "\"child7\":[],\"child8\":[],"
            + "\"child9\":[],\"child10\":[\"SELECTED\"]"
            + "}";
        assertEquals(expected, actual, true);
    }

    @Test
    void shouldCreateAStringWithNullAssignedToChildCountContainerWhenValueIsNull() throws JsonProcessingException {
        ChildSelector childSelector = ChildSelector.builder().childCount(null).build();
        String actual = mapper.writeValueAsString(childSelector);
        String expected = "{"
            + "\"childCountContainer\":null,"
            + "\"child1\":[],\"child2\":[],"
            + "\"child3\":[],\"child4\":[],"
            + "\"child5\":[],\"child6\":[],"
            + "\"child7\":[],\"child8\":[],"
            + "\"child9\":[],\"child10\":[]"
            + "}";
        assertEquals(expected, actual, true);
    }
}
