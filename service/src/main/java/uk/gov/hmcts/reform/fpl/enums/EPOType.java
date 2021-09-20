package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;

@Getter
public enum EPOType {
    REMOVE_TO_ACCOMMODATION("Remove to accommodation", "Symud i lety"),
    PREVENT_REMOVAL("Prevent removal from an address", "Atal symud rhywun o gyfeiriad");

    private final String label;
    private final String welshLabel;

    EPOType(String label, String welshLabel) {
        this.label = label;
        this.welshLabel = welshLabel;
    }

    public String getLabel(Language language) {
        return language == Language.WELSH ? welshLabel : label;
    }
}
