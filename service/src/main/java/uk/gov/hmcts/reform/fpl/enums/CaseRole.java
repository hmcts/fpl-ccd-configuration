package uk.gov.hmcts.reform.fpl.enums;

public enum CaseRole {
    CREATOR,
    LASOLICITOR,
    SOLICITOR,
    LABARRISTER,
    BARRISTER;

    private String formattedName;

    CaseRole() {
        this.formattedName = String.format("[%s]", name());
    }

    public String formattedName() {
        return formattedName;
    }
}
