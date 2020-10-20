package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.exceptions.GrantCaseAccessException;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.assertions.AnnotationAssertion.assertClass;

@ExtendWith(SpringExtension.class)
class CaseRoleServiceTest {

    private static final String CASE_ID = "0";
    private static final String USER_1_ID = "1";
    private static final String USER_2_ID = "2";
    private static final String USER_3_ID = "3";
    private static final String LOCAL_AUTHORITY = "LA1";
    private static final String AUTH_TOKEN = "User token";
    private static final String SERVICE_AUTH_TOKEN = "Service token";
    private static final Set<CaseRole> CASE_ROLES = Set.of(LASOLICITOR, CREATOR);

    @Mock
    private IdamClient idam;

    @Mock
    private CaseUserApi caseUser;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private OrganisationService organisationService;

    @Spy
    private SystemUpdateUserConfiguration userConfig = new SystemUpdateUserConfiguration("SYS", "SYSPASS");

    @InjectMocks
    private CaseRoleService caseRoleService;

    @BeforeEach
    void setup() {
        given(idam.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).willReturn(AUTH_TOKEN);
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
    }

    @Nested
    class GrantCaseAccessToUser {

        @Test
        void shouldGrantCaseAccessToUser() {
            caseRoleService.grantAccessToUser(CASE_ID, USER_1_ID, CASE_ROLES);

            verifyAccessGrantAttempted(USER_1_ID);
        }

        @Test
        void shouldThrowExceptionWhenAccessCannotBeGranted() {
            doThrow(new RuntimeException()).when(caseUser)
                .updateCaseRolesForUser(AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID, USER_1_ID, caseUser(USER_1_ID));

            Exception actualException = assertThrows(Exception.class,
                () -> caseRoleService.grantAccessToUser(CASE_ID, USER_1_ID, CASE_ROLES));

            assertThat(actualException).isEqualTo(new GrantCaseAccessException(CASE_ID, Set.of(USER_1_ID), CASE_ROLES));
        }
    }

    @Nested
    class GrantAccessToLocalAuthority {

        @Test
        void shouldAllowGrantingAccessAsynchronously() {
            assertClass(CaseRoleService.class).hasAsyncMethods("grantAccessToLocalAuthority");
        }

        @Test
        void shouldGrantAccessForEachUser() {
            Set<String> localAuthorityUsers = Set.of(USER_1_ID, USER_2_ID, USER_3_ID);
            Set<String> excludedUser = emptySet();

            when(organisationService.findUserIdsInSameOrganisation(LOCAL_AUTHORITY)).thenReturn(localAuthorityUsers);

            caseRoleService.grantAccessToLocalAuthority(CASE_ID, LOCAL_AUTHORITY, CASE_ROLES, excludedUser);

            verifyAccessGrantAttempted(USER_1_ID);
            verifyAccessGrantAttempted(USER_2_ID);
            verifyAccessGrantAttempted(USER_3_ID);
        }

        @Test
        void shouldGrantAccessForLocalAuthorityUsersWithExclusion() {
            Set<String> localAuthorityUsers = Set.of(USER_1_ID, USER_2_ID, USER_3_ID);
            Set<String> excludedUser = Set.of(USER_2_ID);

            when(organisationService.findUserIdsInSameOrganisation(LOCAL_AUTHORITY)).thenReturn(localAuthorityUsers);

            caseRoleService.grantAccessToLocalAuthority(CASE_ID, LOCAL_AUTHORITY, CASE_ROLES, excludedUser);

            verifyAccessGrantNotAttempted(USER_2_ID);
            verifyAccessGrantAttempted(USER_1_ID);
            verifyAccessGrantAttempted(USER_3_ID);
        }

        @Test
        void shouldGrantAccessToRemainingUsersAndThrowExceptionWhenFailedForSomeUsers() {
            Set<String> localAuthorityUsers = Set.of(USER_1_ID, USER_2_ID, USER_3_ID);
            Set<String> excludedUser = emptySet();

            when(organisationService.findUserIdsInSameOrganisation(LOCAL_AUTHORITY)).thenReturn(localAuthorityUsers);

            doThrow(new RuntimeException())
                .when(caseUser).updateCaseRolesForUser(any(), any(), any(), or(eq(USER_1_ID), eq(USER_2_ID)), any());

            GrantCaseAccessException expectedException =
                new GrantCaseAccessException(CASE_ID, Set.of(USER_1_ID, USER_2_ID), CASE_ROLES);

            GrantCaseAccessException actualException = assertThrows(GrantCaseAccessException.class,
                () -> caseRoleService.grantAccessToLocalAuthority(CASE_ID, LOCAL_AUTHORITY, CASE_ROLES, excludedUser));

            assertThat(actualException).isEqualTo(expectedException);

            verifyAccessGrantAttempted(USER_1_ID);
            verifyAccessGrantAttempted(USER_2_ID);
            verifyAccessGrantAttempted(USER_3_ID);
        }

        @Test
        void shouldThrowsExceptionWhenGrantAccessPreparationFails() {
            when(organisationService.findUserIdsInSameOrganisation(LOCAL_AUTHORITY))
                .thenReturn(Set.of(USER_1_ID, USER_2_ID));

            doThrow(new RuntimeException()).when(idam).getAccessToken(any(), any());

            GrantCaseAccessException expectedException =
                new GrantCaseAccessException(CASE_ID, Set.of(USER_1_ID, USER_2_ID), CASE_ROLES);

            Set<String> excludeUsers = emptySet();

            GrantCaseAccessException actualException = assertThrows(GrantCaseAccessException.class,
                () -> caseRoleService.grantAccessToLocalAuthority(CASE_ID, LOCAL_AUTHORITY, CASE_ROLES, excludeUsers));

            assertThat(actualException).isEqualTo(expectedException);
        }

        @Test
        void shouldThrowsExceptionWhenLocalAuthorityUsersCannotBeFetch() {
            Exception fetchUsersException = new RuntimeException(format("Can not fetch users for %s", LOCAL_AUTHORITY));

            doThrow(new RuntimeException()).when(organisationService).findUserIdsInSameOrganisation(LOCAL_AUTHORITY);

            GrantCaseAccessException expectedException =
                new GrantCaseAccessException(CASE_ID, LOCAL_AUTHORITY, CASE_ROLES, fetchUsersException);

            Set<String> excludeUsers = emptySet();

            GrantCaseAccessException actualException = assertThrows(GrantCaseAccessException.class,
                () -> caseRoleService.grantAccessToLocalAuthority(CASE_ID, LOCAL_AUTHORITY, CASE_ROLES, excludeUsers));

            assertThat(actualException).isEqualTo(expectedException);
        }
    }

    private void verifyAccessGrantAttempted(String userId) {
        verify(caseUser).updateCaseRolesForUser(AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID, userId, caseUser(userId));
    }

    private void verifyAccessGrantNotAttempted(String userId) {
        verify(caseUser, never()).updateCaseRolesForUser(any(), any(), any(), eq(userId), any());
    }

    private CaseUser caseUser(String userId) {
        return new CaseUser(userId, CASE_ROLES.stream().map(CaseRole::formattedName).collect(toSet()));
    }
}
