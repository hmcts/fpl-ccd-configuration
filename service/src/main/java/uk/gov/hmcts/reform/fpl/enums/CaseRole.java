package uk.gov.hmcts.reform.fpl.enums;

public enum CaseRole {
    CREATOR,
    LASOLICITOR,
    EPSMANAGING,
    LAMANAGING,
    SOLICITOR,
    SOLICITOR_A("solicitorA"),
    SOLICITOR_B("solicitorB"),
    SOLICITOR_C("solicitorC"),
    SOLICITOR_D("solicitorD"),
    SOLICITOR_E("solicitorE"),
    SOLICITOR_F("solicitorF"),
    SOLICITOR_G("solicitorG"),
    SOLICITOR_H("solicitorH"),
    SOLICITOR_I("solicitorI"),
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
