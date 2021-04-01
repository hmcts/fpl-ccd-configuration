package uk.gov.hmcts.reform.fpl.enums;

public enum OutsourcingType {
    EPS(CaseRole.EPSMANAGING), MLA(CaseRole.LAMANAGING);

    private final CaseRole caseRole;

    OutsourcingType(CaseRole caseRole) {
        this.caseRole = caseRole;
    }

    public CaseRole getCaseRole() {
        return caseRole;
    }
}
