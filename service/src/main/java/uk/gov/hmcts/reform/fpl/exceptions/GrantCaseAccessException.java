package uk.gov.hmcts.reform.fpl.exceptions;

import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;

import java.util.Set;

@EqualsAndHashCode(callSuper = false)
public class GrantCaseAccessException extends RuntimeException {

    private final String caseId;
    private final String localAuthority;
    private final Set<String> userIds;
    private final Set<CaseRole> caseRoles;

    public GrantCaseAccessException(String caseId, Set<String> userIds, Set<CaseRole> caseRoles) {
        super(String.format("User(s) %s not granted %s to case %s", userIds, caseRoles, caseId));
        this.caseId = caseId;
        this.userIds = userIds;
        this.caseRoles = caseRoles;
        this.localAuthority = null;
    }

    public GrantCaseAccessException(String caseId, String localAuthority, Set<CaseRole> caseRoles, Exception e) {
        super(String.format("Users from %s not granted %s to case %s", localAuthority, caseRoles, caseId), e);
        this.caseId = caseId;
        this.localAuthority = localAuthority;
        this.caseRoles = caseRoles;
        this.userIds = null;
    }
}
