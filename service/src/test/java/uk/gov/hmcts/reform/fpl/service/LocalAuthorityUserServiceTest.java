package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.NoAssociatedUsersException;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class LocalAuthorityUserServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";
    private static final String CASE_ID = "1";
    private static final String[] USER_IDS = {"1", "2", "3"};
    private static final String LOCAL_AUTHORITY = "example";
    private static final Set<String> caseRoles = Set.of("[LASOLICITOR]","[CREATOR]");

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

    @BeforeEach
    void setup() {
        given(client.authenticateUser("fpl-system-update@mailnesia.com", "Password12")).willReturn(AUTH_TOKEN);

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldMakeCallToUpdateCaseRoleEndpointToGrantAccessRolesToUsersWithinLocalAuthority() {
        given(localAuthorityUserLookupConfiguration.getUserIds(LOCAL_AUTHORITY)).willReturn(
            ImmutableList.<String>builder()
                .add(USER_IDS)
                .build()
        );

        localAuthorityUserService.grantUserAccessWithCaseRole(CASE_ID, LOCAL_AUTHORITY);

        verify(caseUserApi, times(1)).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[0]),
            refEq(new CaseUser(USER_IDS[0],caseRoles)));
        verify(caseUserApi, times(1)).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[1]),
            refEq(new CaseUser(USER_IDS[1],caseRoles)));
        verify(caseUserApi, times(1)).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[2]),
            refEq(new CaseUser(USER_IDS[2],caseRoles)));
    }

    @Test
    void shouldThrowCustomExceptionWhenValidLocalAuthorityHasNoUsers() throws IllegalArgumentException {
        given(localAuthorityUserLookupConfiguration.getUserIds(LOCAL_AUTHORITY)).willReturn(
            ImmutableList.<String>builder().build()
        );

        assertThatThrownBy(() ->
            localAuthorityUserService.grantUserAccessWithCaseRole(CASE_ID, LOCAL_AUTHORITY))
            .isInstanceOf(NoAssociatedUsersException.class)
            .hasMessage("No users found for the local authority 'example'");
    }

    @Test
    void shouldNotThrowExceptionWhenCallToUpdateCaseRoleEndpointEndpointFailedForOneOfTheUsers() {
        given(localAuthorityUserLookupConfiguration.getUserIds(LOCAL_AUTHORITY)).willReturn(
            ImmutableList.<String>builder()
                .add(USER_IDS)
                .build()
        );

        willThrow(new RetryableException(500, "Some error", null, null)).given(caseUserApi).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq("1"), refEq(new CaseUser("1",caseRoles)));

        localAuthorityUserService.grantUserAccessWithCaseRole(CASE_ID, LOCAL_AUTHORITY);

        verify(caseUserApi, times(1)).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[0]),
            refEq(new CaseUser(USER_IDS[0], caseRoles)));
        verify(caseUserApi, times(1)).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[1]),
            refEq(new CaseUser(USER_IDS[1], caseRoles)));
        verify(caseUserApi, times(1)).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[2]),
            refEq(new CaseUser(USER_IDS[2], caseRoles)));
    }
}
