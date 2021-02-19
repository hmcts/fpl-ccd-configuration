package uk.gov.hmcts.reform.fpl.exceptions;

import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;

import java.util.Set;

@EqualsAndHashCode(callSuper = false)
public class GrantCaseAccessException extends RuntimeException {

    private final Long caseId;
    private final String localAuthority;
    private final Set<String> userIds;
    private final CaseRole caseRole;

    public GrantCaseAccessException(Long caseId, Set<String> userIds, CaseRole caseRole) {
        super(String.format("User(s) %s not granted %s to case %s", userIds, caseRole, caseId));
        this.caseId = caseId;
        this.userIds = userIds;
        this.caseRole = caseRole;
        this.localAuthority = null;
    }

    public GrantCaseAccessException(Long caseId, String localAuthority, CaseRole caseRole, Exception e) {
        super(String.format("Users from %s not granted %s to case %s", localAuthority, caseRole, caseId), e);
        this.caseId = caseId;
        this.localAuthority = localAuthority;
        this.caseRole = caseRole;
        this.userIds = null;
    }
}
