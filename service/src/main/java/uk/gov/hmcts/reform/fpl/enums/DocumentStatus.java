package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum DocumentStatus {
    ATTACHED("Attached"),
    TO_FOLLOW("To follow"),
    INCLUDED_IN_SWET("Included in social work evidence template (SWET)");

    private final String label;

    DocumentStatus(String label) {
        this.label = label;
    }
}
