package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum GeneratedOrderSubtype {
    INTERIM("Interim"),
    FINAL("Final");

    private final String label;

    public static Optional<GeneratedOrderSubtype> fromType(String type) {
        return Stream.of(GeneratedOrderSubtype.values())
            .filter(subtype -> type.contains(subtype.getLabel()))
            .findFirst();
    }
}
