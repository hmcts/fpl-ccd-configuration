package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.HasLabel;

@Getter
public enum EPOType implements HasLabel {
    REMOVE_TO_ACCOMMODATION("Remove to accommodation"),
    PREVENT_REMOVAL("Prevent removal from an address");

    private final String label;

    EPOType(String label) {
        this.label = label;
    }
}
