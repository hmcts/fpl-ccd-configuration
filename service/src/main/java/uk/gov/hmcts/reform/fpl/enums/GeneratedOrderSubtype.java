package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.types.HasLabel;

@Getter
@RequiredArgsConstructor
public enum GeneratedOrderSubtype implements HasLabel {
    INTERIM("Interim"),
    FINAL("Final");

    private final String label;
}
