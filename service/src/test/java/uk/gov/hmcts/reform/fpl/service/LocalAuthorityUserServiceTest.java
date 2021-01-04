package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;

@ExtendWith(MockitoExtension.class)
class LocalAuthorityUserServiceTest {

    private static final String CASE_ID = "1";
    private static final String LOCAL_AUTHORITY = "example";
    private static final Set<CaseRole> CASE_ROLES = Set.of(LASOLICITOR, CREATOR);

    @Mock
    private CaseRoleService caseRoleService;

    @InjectMocks
    private LocalAuthorityUserService localAuthorityUserService;

    @Test
    void shouldGrantCaseRolesToAllLocalAuthorityUsersWithCaseAssignmentRoles() {
        localAuthorityUserService.grantUserAccessWithCaseRole(CASE_ID, LOCAL_AUTHORITY);

        verify(caseRoleService).grantCaseAssignmentToLocalAuthority(CASE_ID, LOCAL_AUTHORITY, CASE_ROLES);
        verifyNoMoreInteractions(caseRoleService);
    }
}
