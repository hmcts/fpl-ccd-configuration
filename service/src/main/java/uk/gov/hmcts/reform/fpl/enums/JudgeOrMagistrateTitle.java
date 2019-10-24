package uk.gov.hmcts.reform.fpl.enums;

public enum JudgeOrMagistrateTitle {
    HER_HONOUR_JUDGE("Her Honour Judge"),
    HIS_HONOUR_JUDGE("His Honour Judge"),
    DEPUTY_DISTRICT_JUDGE("Deputy District Judge"),
    MAGISTRATES("Magistrates (JP)");

    private final String label;

    JudgeOrMagistrateTitle(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
