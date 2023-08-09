package uk.gov.hmcts.reform.fpl.config.utils;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LookupConfigParserTest {

    @Test
    void parseStringValueShouldThrowExceptionWhenMappingIsEmpty() {
        assertThatThrownBy(() -> {
            LookupConfigParser.parseStringValue("");
        }).isInstanceOf(IllegalArgumentException.class).hasMessage("Mapping configuration cannot be empty");
    }

    @Test
    void parseStringValueShouldReturnCorrectLookupTableWhenOnlyOneMappingExists() {
        Map<String, String> result = LookupConfigParser.parseStringValue("SA=>Swansea");

        assertThat(result)
            .hasSize(1)
            .containsEntry("SA", "Swansea");
    }

    @Test
    void parseStringValueShouldReturnCorrectLookupTableWhenMoreThenOneMappingExists() {
        Map<String, String> result = LookupConfigParser.parseStringValue("SA=>Swansea;HN=>Hillingdon");

        assertThat(result)
            .hasSize(2)
            .containsEntry("SA", "Swansea")
            .containsEntry("HN", "Hillingdon");
    }

    @Test
    void parseStringListValue() {
        assertThatThrownBy(() -> {
            LookupConfigParser.parseStringListValue("");
        }).isInstanceOf(IllegalArgumentException.class).hasMessage("Mapping configuration cannot be empty");
    }

    @Test
    void parseStringListValueShouldReturnCorrectLookupTableWhenOnlyOneMappingExists() {
        Map<String, List<String>> result = LookupConfigParser.parseStringListValue("SA=>1,2|3|4");

        assertThat(result)
            .hasSize(1)
            .containsEntry("SA", ImmutableList.of("1,2", "3", "4"));
    }

    @Test
    void parseStringListValueShouldReturnCorrectLookupTableWhenMoreThenOneMappingExists() {
        Map<String, List<String>> result = LookupConfigParser.parseStringListValue("SA=>1|2|3;HN=>4|5, 6|7");

        assertThat(result)
            .hasSize(2)
            .containsEntry("SA", ImmutableList.of("1", "2", "3"))
            .containsEntry("HN", ImmutableList.of("4", "5, 6", "7"));
    }
}
