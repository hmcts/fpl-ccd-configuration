package uk.gov.hmcts.reform.fpl.enums;

import java.util.List;
import java.util.stream.Stream;

public enum CaseRole {
    CREATOR,
    LASOLICITOR,
    EPSMANAGING,
    LAMANAGING,
    LASHARED,
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
        return CaseRole.valueOf(name.replaceAll("[\\[\\]]", ""));
    }

    public static List<CaseRole> representativeSolicitors() {
        return Stream.concat(List.of(CAFCASSSOLICITOR).stream(),
            Stream.concat(respondentSolicitors().stream(), childSolicitors().stream())
        ).toList();
    }

    public static List<CaseRole> barristers() {
        return List.of(LABARRISTER, BARRISTER);
    }

    public static List<CaseRole> respondentSolicitors() {
        return List.of(SOLICITORA, SOLICITORB, SOLICITORC, SOLICITORD, SOLICITORE, SOLICITORF, SOLICITORG, SOLICITORH,
            SOLICITORI, SOLICITORJ,
            SOLICITOR);
    }

    public static List<CaseRole> childSolicitors() {
        return List.of(CHILDSOLICITORA, CHILDSOLICITORB, CHILDSOLICITORC, CHILDSOLICITORD, CHILDSOLICITORE,
            CHILDSOLICITORF, CHILDSOLICITORG, CHILDSOLICITORH, CHILDSOLICITORI, CHILDSOLICITORJ, CHILDSOLICITORK,
            CHILDSOLICITORL, CHILDSOLICITORM, CHILDSOLICITORN, CHILDSOLICITORO);
    }

    public static List<CaseRole> designatedLASolicitors() {
        return List.of(LASOLICITOR, EPSMANAGING, LAMANAGING, LABARRISTER);
    }

    public static List<CaseRole> secondaryLASolicitors() {
        return List.of(LASHARED);
    }

    private static String formatName(String name) {
        return String.format("[%s]", name);
    }

    public static CaseRole getByIndex(String enumPrefix, int index) {
        char enumChar = (char) ('A' + index);
        String enumName = enumPrefix + enumChar;
        return CaseRole.from(enumName);
    }
}
