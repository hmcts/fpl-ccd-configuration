package uk.gov.hmcts.reform.fpl.json.deserializer;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.fpl.enums.YesNo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YesNoDeserializerTest extends DeserializerTest {

    YesNoDeserializerTest() {
        super(YesNo.class, new YesNoDeserializer());
    }

    @ParameterizedTest
    @NullAndEmptySource()
    void shouldReturnNullWhenValueIsNullOrEmpty(String value) throws Exception {
        final TestClass actual = mapper.readValue(json(value), TestClass.class);
        assertThat(actual.test).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"yes", "Yes", "No", "no", "a", "b"})
    void shouldThrowsExceptionWhenValueDoesNotRepresentEnum(String value) {
        assertThatThrownBy(() -> mapper.readValue(json(value), TestClass.class))
            .isInstanceOf(JsonMappingException.class)
            .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldDeserializeYesValue() throws Exception {
        final TestClass actual = mapper.readValue(json("YES"), TestClass.class);

        assertThat(actual.test).isEqualTo(YesNo.YES);
    }

    @Test
    void shouldDeserializeNoValue() throws Exception {
        final TestClass actual = mapper.readValue(json("NO"), TestClass.class);

        assertThat(actual.test).isEqualTo(YesNo.NO);
    }

    private static class TestClass {
        public YesNo test;
    }

    private static String json(String value) {
        return new JSONObject()
            .put("test", value)
            .toString();
    }

}


