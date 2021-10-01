package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum AddressNotKnowReason {
    NO_FIXED_ABODE("No fixed abode"),
    DECEASED("Person deceased");

    private final String type;

    public static Optional<AddressNotKnowReason> fromType(String value) {
        return Stream.of(AddressNotKnowReason.values())
            .filter(gender -> gender.getType().equalsIgnoreCase(value))
            .findFirst();
    }
}
