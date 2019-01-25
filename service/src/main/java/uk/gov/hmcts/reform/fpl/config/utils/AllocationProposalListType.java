package uk.gov.hmcts.reform.fpl.config.utils;

public enum AllocationProposalListType {

    LAY_JUSTICES("Lay justices"),
    DISTRICT_JUDGE("District judge"),
    CIRCUIT_JUDGE("Circuit judge"),
    SECTION_9_CIRCUIT_JUDGE("Section 9 circuit judge"),
    HIGH_COURT_JUDGE("High court judge");

    private final String label;

    AllocationProposalListType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
