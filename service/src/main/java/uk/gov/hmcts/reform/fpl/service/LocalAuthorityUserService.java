package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;

import java.util.Set;

import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalAuthorityUserService {

    private static final Set<CaseRole> CASE_ROLES = Set.of(LASOLICITOR, CREATOR);
    private final CaseRoleService caseRoleService;

    public void grantUserAccessWithCaseRole(String caseId, String caseLocalAuthority) {
        caseRoleService.grantCaseAssignmentToLocalAuthority(caseId, caseLocalAuthority, CASE_ROLES);
    }
}
