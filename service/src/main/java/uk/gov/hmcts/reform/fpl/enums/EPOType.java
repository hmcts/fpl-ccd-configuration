package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;

@Getter
public enum EPOType {
    REMOVE_TO_ACCOMMODATION("Remove to accommodation"),
    PREVENT_REMOVAL("Prevent removal from an address");

    private final String label;

    EPOType(String label) {
        this.label = label;
    }
}
