package uk.gov.hmcts.reform.fpl.config.utils;

public enum EmergencyProtectionOrderReasonsType {

    PLACED_IN_PROVIDED_ACCOMMODATION("There???s reasonable cause to believe the child is likely to suffer"
        + " significant harm if they???re not moved to accommodation provided by you, or on your behalf"),
    KEPT_IN_CURRENT_ACCOMMODATION("There???s reasonable cause to believe the child is likely to suffer significant"
        + " harm if they don???t stay in their current accommodation"),
    URGENT_ACCESS_TO_CHILD("You???re making enquiries and need urgent access to the child to find out about their"
        + " welfare, and access is being unreasonably refused");

    private final String label;

    EmergencyProtectionOrderReasonsType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
