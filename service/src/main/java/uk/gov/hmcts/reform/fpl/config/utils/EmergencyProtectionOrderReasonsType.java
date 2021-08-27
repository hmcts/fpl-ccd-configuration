package uk.gov.hmcts.reform.fpl.config.utils;

import uk.gov.hmcts.reform.fpl.model.configuration.Language;

public enum EmergencyProtectionOrderReasonsType {

    HARM_IF_NOT_MOVED_TO_NEW_ACCOMMODATION(
        "There’s reasonable cause to believe the child is likely to suffer significant harm if they’re not moved"
            + " to accommodation provided by you, or on your behalf",
        "There’s reasonable cause to believe the child is likely to suffer significant harm if they’re not moved"
            + " to accommodation provided by you, or on your behalf"),
    HARM_IF_KEPT_IN_CURRENT_ACCOMMODATION("There’s reasonable cause to believe the child is likely to"
        + " suffer significant harm if they don’t stay in their current accommodation",
        "There’s reasonable cause to believe the child is likely to"
            + " suffer significant harm if they don’t stay in their current accommodation"),
    URGENT_ACCESS_TO_CHILD_IS_OBSTRUCTED("You’re making enquiries and need urgent access to the child to find out"
        + " about their welfare, and access is being unreasonably refused",
        "You’re making enquiries and need urgent access to the child to find out"
            + " about their welfare, and access is being unreasonably refused");

    private final String label;
    private final String welshLabel;

    EmergencyProtectionOrderReasonsType(String label, String welshLabel) {
        this.label = label;
        this.welshLabel = welshLabel;
    }

    public String getLabel() {
        return label;
    }

    public String getLabel(Language language) {
        return language == Language.WELSH ? welshLabel : label;
    }

}
