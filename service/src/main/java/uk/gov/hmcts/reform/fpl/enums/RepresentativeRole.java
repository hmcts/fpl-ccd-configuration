package uk.gov.hmcts.reform.fpl.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CAFCASSSOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LABARRISTER;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.SOLICITOR;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum RepresentativeRole {
    @CCD(label = "LA Legal Representative")
    LA_LEGAL_REPRESENTATIVE(Type.LASOLICITOR, LASOLICITOR),
    @CCD(label = "LA Barrister")
    LA_BARRISTER(Type.LABARRISTER, LABARRISTER),
    @CCD(label = "Barrister")
    BARRISTER(Type.BARRISTER, CaseRole.BARRISTER),
    @CCD(label = "CAFCASS Guardian")
    CAFCASS_GUARDIAN(Type.CAFCASS),
    @CCD(label = "CAFCASS Solicitor")
    CAFCASS_SOLICITOR(Type.CAFCASS, CAFCASSSOLICITOR),
    @CCD(label = "Representing respondent 1")
    REPRESENTING_RESPONDENT_1(Type.RESPONDENT, 0, SOLICITOR),
    @CCD(label = "Representing respondent 2")
    REPRESENTING_RESPONDENT_2(Type.RESPONDENT, 1, SOLICITOR),
    @CCD(label = "Representing respondent 3")
    REPRESENTING_RESPONDENT_3(Type.RESPONDENT, 2, SOLICITOR),
    @CCD(label = "Representing respondent 4")
    REPRESENTING_RESPONDENT_4(Type.RESPONDENT, 3, SOLICITOR),
    @CCD(label = "Representing respondent 5")
    REPRESENTING_RESPONDENT_5(Type.RESPONDENT, 4, SOLICITOR),
    @CCD(label = "Representing respondent 6")
    REPRESENTING_RESPONDENT_6(Type.RESPONDENT, 5, SOLICITOR),
    @CCD(label = "Representing respondent 7")
    REPRESENTING_RESPONDENT_7(Type.RESPONDENT, 6, SOLICITOR),
    @CCD(label = "Representing respondent 8")
    REPRESENTING_RESPONDENT_8(Type.RESPONDENT, 7, SOLICITOR),
    @CCD(label = "Representing respondent 9")
    REPRESENTING_RESPONDENT_9(Type.RESPONDENT, 8, SOLICITOR),
    @CCD(label = "Representing respondent 10")
    REPRESENTING_RESPONDENT_10(Type.RESPONDENT, 9, SOLICITOR),
    @CCD(label = "Representing person 1")
    REPRESENTING_PERSON_1(Type.OTHER, 0, SOLICITOR),
    @CCD(label = "Representing other person 1")
    REPRESENTING_OTHER_PERSON_1(Type.OTHER, 1, SOLICITOR),
    @CCD(label = "Representing other person 2")
    REPRESENTING_OTHER_PERSON_2(Type.OTHER, 2, SOLICITOR),
    @CCD(label = "Representing other person 3")
    REPRESENTING_OTHER_PERSON_3(Type.OTHER, 3, SOLICITOR),
    @CCD(label = "Representing other person 4")
    REPRESENTING_OTHER_PERSON_4(Type.OTHER, 4, SOLICITOR),
    @CCD(label = "Representing other person 5")
    REPRESENTING_OTHER_PERSON_5(Type.OTHER, 5, SOLICITOR),
    @CCD(label = "Representing other person 6")
    REPRESENTING_OTHER_PERSON_6(Type.OTHER, 6, SOLICITOR),
    @CCD(label = "Representing other person 7")
    REPRESENTING_OTHER_PERSON_7(Type.OTHER, 7, SOLICITOR),
    @CCD(label = "Representing other person 8")
    REPRESENTING_OTHER_PERSON_8(Type.OTHER, 8, SOLICITOR),
    @CCD(label = "Representing other person 9")
    REPRESENTING_OTHER_PERSON_9(Type.OTHER, 9, SOLICITOR);

    private Set<CaseRole> caseRoles = new HashSet<>();
    private Type type;
    private Integer sequenceNo;

    RepresentativeRole(Type type, Integer sequenceNo, CaseRole... caseRoles) {
        this.type = type;
        this.sequenceNo = sequenceNo;
        this.caseRoles.addAll(Arrays.asList(caseRoles));
    }

    RepresentativeRole(Type type, CaseRole... caseRoles) {
        this(type, null, caseRoles);
    }

    public Set<CaseRole> getCaseRoles() {
        return new HashSet<>(caseRoles);
    }

    public Type getType() {
        return type;
    }

    public Integer getSequenceNo() {
        return sequenceNo;
    }

    public enum Type {
        OTHER, RESPONDENT, CAFCASS, LASOLICITOR, LABARRISTER, BARRISTER
    }
}
