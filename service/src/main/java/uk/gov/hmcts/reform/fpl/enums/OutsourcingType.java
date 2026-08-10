package uk.gov.hmcts.reform.fpl.enums;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "CaseOutsourcingType", generate = true)
public enum OutsourcingType {
    @CCD(label = "External Private Solicitor")
    EPS(CaseRole.EPSMANAGING),
    @CCD(label = "Managed by Local Authority")
    MLA(CaseRole.LAMANAGING);

    private final CaseRole caseRole;

    OutsourcingType(CaseRole caseRole) {
        this.caseRole = caseRole;
    }

    public CaseRole getCaseRole() {
        return caseRole;
    }
}
