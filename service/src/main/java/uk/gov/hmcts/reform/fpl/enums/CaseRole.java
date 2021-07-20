package uk.gov.hmcts.reform.fpl.enums;

import java.util.List;

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
    CAFCASSSOLICITOR;

    private final String formattedName;

    CaseRole() {
        this.formattedName = formatName(name());
    }

    public String formattedName() {
        return formattedName;
    }

    public static CaseRole from(String name) {
        return CaseRole.valueOf(name.replaceAll("[\\[\\]]", ""));
    }

    //Add child solicitor roles here
    public static List<CaseRole> representativeSolicitors() {
        return List.of(SOLICITORA, SOLICITORB, SOLICITORC, SOLICITORD, SOLICITORE, SOLICITORF, SOLICITORG, SOLICITORH,
            SOLICITORI, SOLICITORJ);
    }

    private static String formatName(String name) {
        return String.format("[%s]", name);
    }
}
