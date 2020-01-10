package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum GeneratedOrderSubtype {
    INTERIM("Interim"),
    FINAL("Final");


    private final String label;

    GeneratedOrderSubtype(String label) {
        this.label = label;
    }
}
