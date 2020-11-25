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
    CASE_MANAGEMENT("PREPARE_FOR_HEARING", "Case management"),

    CLOSED("CLOSED", "Closed"),

    @JsonProperty("Deleted")
    DELETED("Deleted"),

    RETURNED("RETURNED", "Returned"),

    FINAL_HEARING("FINAL_HEARING", "Final hearing");

    private final String value;
    private final String label;

    State(String value) {
        this.value = value;
        this.label = value;
    }

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
