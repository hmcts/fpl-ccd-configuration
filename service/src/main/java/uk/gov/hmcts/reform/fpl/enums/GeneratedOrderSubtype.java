package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GeneratedOrderSubtype {
    INTERIM("Interim"),
    FINAL("Final");

    private final String label;
}
