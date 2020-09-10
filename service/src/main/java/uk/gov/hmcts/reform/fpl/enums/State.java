package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum State {
    @JsonProperty("Open")
    OPEN("Open"),

    @JsonProperty("Submitted")
    SUBMITTED("Submitted"),

    @JsonProperty("Gatekeeping")
    GATEKEEPING("Gatekeeping"),

    // State label renamed to 'Case management' as of FPLA-1920.
    // State ID remains 'PREPARE_FOR_HEARING' to avoid breaking existing cases.
    @JsonProperty("PREPARE_FOR_HEARING")
    CASE_MANAGEMENT("PREPARE_FOR_HEARING"),

    CLOSED("CLOSED"),

    @JsonProperty("Deleted")
    DELETED("Deleted"),

    RETURNED("RETURNED"),

    ISSUE_RESOLUTION("ISSUE_RESOLUTION"),

    FINAL_HEARING("FINAL_HEARING");

    private final String value;

    public static State fromValue(final String value) {
        return tryFromValue(value)
            .orElseThrow(() -> new NoSuchElementException("Unable to map " + value + " to a case state"));
    }

    public static Optional<State> tryFromValue(final String value) {
        return Stream.of(values())
            .filter(state -> state.value.equalsIgnoreCase(value))
            .findFirst();
    }
}
