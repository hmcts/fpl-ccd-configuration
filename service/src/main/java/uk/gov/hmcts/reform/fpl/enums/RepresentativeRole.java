package uk.gov.hmcts.reform.fpl.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.SOLICITOR;

public enum RepresentativeRole {
    LA_LEGAL_REPRESENTATIVE(Type.LASOLICITOR, LASOLICITOR),
    CAFCASS_GUARDIAN(Type.CAFCASS),
    CAFCASS_SOLICITOR(Type.CAFCASS),
    REPRESENTING_RESPONDENT_1(Type.RESPONDENT, 0, SOLICITOR),
    REPRESENTING_RESPONDENT_2(Type.RESPONDENT, 1, SOLICITOR),
    REPRESENTING_RESPONDENT_3(Type.RESPONDENT, 2, SOLICITOR),
    REPRESENTING_RESPONDENT_4(Type.RESPONDENT, 3, SOLICITOR),
    REPRESENTING_RESPONDENT_5(Type.RESPONDENT, 4, SOLICITOR),
    REPRESENTING_RESPONDENT_6(Type.RESPONDENT, 5, SOLICITOR),
    REPRESENTING_RESPONDENT_7(Type.RESPONDENT, 6, SOLICITOR),
    REPRESENTING_RESPONDENT_8(Type.RESPONDENT, 7, SOLICITOR),
    REPRESENTING_RESPONDENT_9(Type.RESPONDENT, 8, SOLICITOR),
    REPRESENTING_RESPONDENT_10(Type.RESPONDENT, 9, SOLICITOR),
    REPRESENTING_PERSON_1(Type.OTHER, 0, SOLICITOR),
    REPRESENTING_OTHER_PERSON_1(Type.OTHER, 1, SOLICITOR),
    REPRESENTING_OTHER_PERSON_2(Type.OTHER, 2, SOLICITOR),
    REPRESENTING_OTHER_PERSON_3(Type.OTHER, 3, SOLICITOR),
    REPRESENTING_OTHER_PERSON_4(Type.OTHER, 4, SOLICITOR),
    REPRESENTING_OTHER_PERSON_5(Type.OTHER, 5, SOLICITOR),
    REPRESENTING_OTHER_PERSON_6(Type.OTHER, 6, SOLICITOR),
    REPRESENTING_OTHER_PERSON_7(Type.OTHER, 7, SOLICITOR),
    REPRESENTING_OTHER_PERSON_8(Type.OTHER, 8, SOLICITOR),
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
        OTHER, RESPONDENT, CAFCASS, LASOLICITOR
    }
}
