package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class LocalAuthorityUserServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";
    private static final String CASE_ID = "1";
    private static final String CREATOR_USER_ID = "1";
    private static final String LOCAL_AUTHORITY = "example";

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseUserApi caseUserApi;
    @Mock
    private LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration;

    @Mock
    private IdamClient client;

    @InjectMocks
    private LocalAuthorityUserService localAuthorityUserService;

    @Test
    void shouldMakeCallToUpdateCaseRoleEndpointToGrantAccessRolesToUsersWithinLocalAuthority() {
        String additionalUserId = "1";
        Set<String> caseRoles = Set.of("[LASOLICITOR]","[CREATOR]");

        given(client.authenticateUser("fpl-system-update@mailnesia.com", "Password12")).willReturn(AUTH_TOKEN);
        given(localAuthorityUserLookupConfiguration.getUserIds(LOCAL_AUTHORITY)).willReturn(
            ImmutableList.<String>builder()
                .add(CREATOR_USER_ID, additionalUserId)
                .build()
        );

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

        localAuthorityUserService.grantUserAccessWithCaseRole(CREATOR_USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verify(caseUserApi, times(2)).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(CREATOR_USER_ID),
            refEq(new CaseUser(additionalUserId,caseRoles)));
    }
}
