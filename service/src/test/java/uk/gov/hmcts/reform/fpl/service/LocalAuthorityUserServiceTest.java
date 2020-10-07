package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;

@ExtendWith(MockitoExtension.class)
class LocalAuthorityUserServiceTest {

    private static final String CASE_ID = "1";
    private static final String USER_ID = "2";
    private static final String LOCAL_AUTHORITY = "example";
    private static final Set<CaseRole> CASE_ROLES = Set.of(LASOLICITOR, CREATOR);

    @Mock
    private RequestData requestData;

    @Mock
    private CaseRoleService caseRoleService;

    @Mock
    private FeatureToggleService featureToggleService;


    @InjectMocks
    private LocalAuthorityUserService localAuthorityUserService;

    @BeforeEach
    void setup() {
        given(requestData.userId()).willReturn(USER_ID);
    }

    @Test
    void shouldGrantCaseRolesToAllLocalAuthorityUsers() {
        localAuthorityUserService.grantUserAccessWithCaseRole(CASE_ID, LOCAL_AUTHORITY);

        verify(caseRoleService).grantAccessToUser(CASE_ID, USER_ID, CASE_ROLES);
        verify(caseRoleService).grantAccessToLocalAuthority(CASE_ID, LOCAL_AUTHORITY, CASE_ROLES, Set.of(USER_ID));
    }
}
