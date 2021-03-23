package uk.gov.hmcts.reform.fpl.enums;

public enum CaseRole {
    CREATOR,
    LASOLICITOR,
    EPSMANAGING,
    LAMANAGING,
    SOLICITOR,
    // Disabling names as ALL CAPS makes this unreadable
    solicitorA, //NOSONAR
    solicitorB, //NOSONAR
    solicitorC, //NOSONAR
    solicitorD, //NOSONAR
    solicitorE, //NOSONAR
    solicitorF, //NOSONAR
    solicitorG, //NOSONAR
    solicitorH, //NOSONAR
    solicitorI, //NOSONAR
    LABARRISTER,
    BARRISTER,
    CAFCASSSOLICITOR;

    private String formattedName;

    CaseRole() {
        this.formattedName = String.format("[%s]", name());
    }

    public String formattedName() {
        return formattedName;
    }

    public static CaseRole from(String name) {
        return CaseRole.valueOf(name.replaceAll("[\\[\\]]", ""));
    }
}
