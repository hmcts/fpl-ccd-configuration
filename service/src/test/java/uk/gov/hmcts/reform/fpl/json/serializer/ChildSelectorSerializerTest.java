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
    void shouldSerialiseFalseToEmptyArray() throws JsonProcessingException {
        ChildSelector childSelector = ChildSelector.builder().build();
        String actual = mapper.writeValueAsString(childSelector);
        String expected = "{" +
            "\"childCountContainer\":\"\"," +
            "\"child1\":[],\"child2\":[]," +
            "\"child3\":[],\"child4\":[]," +
            "\"child5\":[],\"child6\":[]," +
            "\"child7\":[],\"child8\":[]," +
            "\"child9\":[],\"child10\":[]" +
            "}";
        assertEquals(expected, actual, false);
    }

    @Test
    void shouldSerialiseTrueToPopulatedArray() throws JsonProcessingException {
        ChildSelector childSelector = ChildSelector.builder().child1(true).child5(true).child10(true).build();
        String actual = mapper.writeValueAsString(childSelector);
        String expected = "{" +
            "\"childCountContainer\":\"\"," +
            "\"child1\":[\"selected\"],\"child2\":[]," +
            "\"child3\":[],\"child4\":[]," +
            "\"child5\":[\"selected\"],\"child6\":[]," +
            "\"child7\":[],\"child8\":[]," +
            "\"child9\":[],\"child10\":[\"selected\"]" +
            "}";
        assertEquals(expected, actual, false);
    }

    @Test
    void shouldSerialiseNullStringToNull() throws JsonProcessingException {
        ChildSelector childSelector = ChildSelector.builder().childCountContainer(null).build();
        String actual = mapper.writeValueAsString(childSelector);
        String expected = "{" +
            "\"childCountContainer\":null," +
            "\"child1\":[],\"child2\":[]," +
            "\"child3\":[],\"child4\":[]," +
            "\"child5\":[],\"child6\":[]," +
            "\"child7\":[],\"child8\":[]," +
            "\"child9\":[],\"child10\":[]" +
            "}";
        assertEquals(expected, actual, false);
    }
}
