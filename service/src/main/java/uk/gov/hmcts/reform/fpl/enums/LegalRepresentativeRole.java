package uk.gov.hmcts.reform.fpl.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LABARRISTER;

public enum LegalRepresentativeRole {
    EXTERNAL_LA_SOLICITOR(LABARRISTER),
    EXTERNAL_LA_BARRISTER(LABARRISTER);

    private final Set<CaseRole> caseRoles = new HashSet<>();

    LegalRepresentativeRole(CaseRole... caseRoles) {
        this.caseRoles.addAll(Arrays.asList(caseRoles));
    }

    public Set<CaseRole> getCaseRoles() {
        return new HashSet<>(caseRoles);
    }
}
