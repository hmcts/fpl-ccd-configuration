package uk.gov.hmcts.reform.fpl.enums;

public enum CaseRole {
    CREATOR,
    LASOLICITOR,
    EPSMANAGING,
    LAMANAGING,
    SOLICITOR,
    SOLICITOR_A("SOLICITORA"),
    SOLICITOR_B("SOLICITORB"),
    SOLICITOR_C("SOLICITORC"),
    SOLICITOR_D("SOLICITORD"),
    SOLICITOR_E("SOLICITORE"),
    SOLICITOR_F("SOLICITORF"),
    SOLICITOR_G("SOLICITORG"),
    SOLICITOR_H("SOLICITORH"),
    SOLICITOR_I("SOLICITORI"),
    SOLICITOR_J("SOLICITORJ"),
    LABARRISTER,
    BARRISTER,
    CAFCASSSOLICITOR;

    private final String formattedName;

    CaseRole() {
        this.formattedName = formatName(name());
    }

    CaseRole(String name) {
        this.formattedName = formatName(name);
    }

    public String formattedName() {
        return formattedName;
    }

    public static CaseRole from(String name) {
        return CaseRole.valueOf(name.replaceAll("[\\[\\]]", "").replace("solicitor", "SOLICITOR_"));
    }

    private static String formatName(String name) {
        return String.format("[%s]", name);
    }
}
