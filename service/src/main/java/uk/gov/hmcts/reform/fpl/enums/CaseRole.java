package uk.gov.hmcts.reform.fpl.enums;

import java.util.List;
import java.util.stream.Stream;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum CaseRole {
    @CCD(label = "Creator")
    CREATOR,
    @CCD(label = "LA Solicitor")
    LASOLICITOR,
    @CCD(label = "EPS Managing")
    EPSMANAGING,
    @CCD(label = "LA Managing")
    LAMANAGING,
    @CCD(label = "LA Shared")
    LASHARED,
    @CCD(label = "Solicitor")
    SOLICITOR,
    @CCD(label = "Solicitor A")
    SOLICITORA,
    @CCD(label = "Solicitor B")
    SOLICITORB,
    @CCD(label = "Solicitor C")
    SOLICITORC,
    @CCD(label = "Solicitor D")
    SOLICITORD,
    @CCD(label = "Solicitor E")
    SOLICITORE,
    @CCD(label = "Solicitor F")
    SOLICITORF,
    @CCD(label = "Solicitor G")
    SOLICITORG,
    @CCD(label = "Solicitor H")
    SOLICITORH,
    @CCD(label = "Solicitor I")
    SOLICITORI,
    @CCD(label = "Solicitor J")
    SOLICITORJ,
    @CCD(label = "La Barrister")
    LABARRISTER,
    @CCD(label = "Barrister")
    BARRISTER,
    @CCD(label = "Cafcass Solicitor")
    CAFCASSSOLICITOR,
    // Child representative solicitors
    @CCD(label = "Child Solicitor A")
    CHILDSOLICITORA,
    @CCD(label = "Child Solicitor B")
    CHILDSOLICITORB,
    @CCD(label = "Child Solicitor C")
    CHILDSOLICITORC,
    @CCD(label = "Child Solicitor D")
    CHILDSOLICITORD,
    @CCD(label = "Child Solicitor E")
    CHILDSOLICITORE,
    @CCD(label = "Child Solicitor F")
    CHILDSOLICITORF,
    @CCD(label = "Child Solicitor G")
    CHILDSOLICITORG,
    @CCD(label = "Child Solicitor H")
    CHILDSOLICITORH,
    @CCD(label = "Child Solicitor I")
    CHILDSOLICITORI,
    @CCD(label = "Child Solicitor J")
    CHILDSOLICITORJ,
    @CCD(label = "Child Solicitor K")
    CHILDSOLICITORK,
    @CCD(label = "Child Solicitor L")
    CHILDSOLICITORL,
    @CCD(label = "Child Solicitor M")
    CHILDSOLICITORM,
    @CCD(label = "Child Solicitor N")
    CHILDSOLICITORN,
    @CCD(label = "Child Solicitor O")
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
