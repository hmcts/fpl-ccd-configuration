package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicListDeserializerTest extends DeserializerTest {

    DynamicListDeserializerTest() {
        super(DynamicList.class, new DynamicListDeserializer());
    }

    @Test
    void shouldDeserializeFromValueString() throws JsonProcessingException {
        final String jsonString = "{\"dynamicList\":\"test\"}";

        final DynamicList actualDynamicList = mapper.readValue(jsonString, DynamicListContainer.class).dynamicList;

        final DynamicList expectedDynamicList = DynamicList.builder()
            .value(dynamicListElement("test", "test"))
            .listItems(List.of(dynamicListElement("test", "test")))
            .build();

        assertThat(actualDynamicList).isEqualTo(expectedDynamicList);
    }

    @Test
    void shouldDeserializeFromObject() throws JsonProcessingException {
        final String jsonString = new JSONObject().put("dynamicList", new JSONObject()
            .put("value", dynamicListItem("testCode", "testLabel"))
            .put("list_items", List.of(
                dynamicListItem("testCode", "testLabel"),
                dynamicListItem("testCode1", "testLabel1"))))
            .toString();

        final DynamicList actualDynamicList = mapper.readValue(jsonString, DynamicListContainer.class).dynamicList;

        final DynamicList expectedDynamicList = DynamicList.builder()
            .value(dynamicListElement("testCode", "testLabel"))
            .listItems(List.of(
                dynamicListElement("testCode", "testLabel"),
                dynamicListElement("testCode1", "testLabel1")))
            .build();

        assertThat(actualDynamicList).isEqualTo(expectedDynamicList);
    }

    private DynamicListElement dynamicListElement(String code, String value) {
        return DynamicListElement.builder()
            .code(code)
            .label(value)
            .build();
    }

    private JSONObject dynamicListItem(String code, String value) {
        return new JSONObject()
            .put("code", code)
            .put("label", value);
    }

    /**
     * DynamicListDeserializer works with fields annotated with @JsonDeserialize.
     * It is dues to lombok @Jacksonized present on DynamicList class that adds its custom deserializer at compilation
     * time and overrides default one.
     */
    static class DynamicListContainer {
        @JsonDeserialize(using = DynamicListDeserializer.class)
        DynamicList dynamicList;
    }
}
