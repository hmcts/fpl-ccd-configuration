package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.reform.am.model.RoleCategory;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum OrganisationalRole {

    JUDGE("judge", RoleCategory.JUDICIAL),
    FEE_PAID_JUDGE("fee-paid-judge", RoleCategory.JUDICIAL),
    LEADERSHIP_JUDGE("leadership-judge", RoleCategory.JUDICIAL),

    CTSC("ctsc", RoleCategory.CTSC),
    CTSC_TEAM_LEADER("ctsc-team-leader", RoleCategory.CTSC),

    LOCAL_COURT_ADMIN("hearing-centre-admin", RoleCategory.ADMIN),
    LOCAL_COURT_TEAM_LEADER("hearing-centre-team-leader", RoleCategory.ADMIN),

    LEGAL_ADVISER("tribunal-caseworker", RoleCategory.LEGAL_OPERATIONS),
    SENIOR_LEGAL_ADVISER("senior-tribunal-caseworker", RoleCategory.LEGAL_OPERATIONS);

    public final String value;
    public final RoleCategory roleCategory;

    public static Optional<OrganisationalRole> from(String label) {
        return Arrays.stream(OrganisationalRole.values())
            .filter(role -> role.getValue().equals(label))
            .findFirst();
    }

}
