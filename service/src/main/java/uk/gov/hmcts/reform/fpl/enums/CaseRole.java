package uk.gov.hmcts.reform.fpl.enums;

public enum CaseRole {
    CREATOR,
    LASOLICITOR,
    EPSMANAGING,
    LAMANAGING,
    SOLICITOR,
    SOLICITORA,
    SOLICITORB,
    SOLICITORC,
    SOLICITORD,
    SOLICITORE,
    SOLICITORF,
    SOLICITORG,
    SOLICITORH,
    SOLICITORI,
    SOLICITORJ,
    LABARRISTER,
    BARRISTER,
    CAFCASSSOLICITOR,
    // Child representative solicitors
    CHILDSOLICITORA,
    CHILDSOLICITORB,
    CHILDSOLICITORC,
    CHILDSOLICITORD,
    CHILDSOLICITORE,
    CHILDSOLICITORF,
    CHILDSOLICITORG,
    CHILDSOLICITORH,
    CHILDSOLICITORI,
    CHILDSOLICITORJ,
    CHILDSOLICITORK,
    CHILDSOLICITORL,
    CHILDSOLICITORM,
    CHILDSOLICITORN,
    CHILDSOLICITORO;

    private final String formattedName;

    CaseRole() {
        this.formattedName = formatName(name());
    }

    public String formattedName() {
        return formattedName;
    }

    public static CaseRole from(String name) {
        return CaseRole.valueOf(name.replaceAll("[\\[\\]]", "").replace("SOLICITOR", "SOLICITOR"));
    }

    private static String formatName(String name) {
        return String.format("[%s]", name);
    }
}
