package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
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
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, SystemUpdateUserConfiguration.class})
class LocalAuthorityUserServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "3";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";
    private static final String CASE_ID = "1";
    private static final String[] USER_IDS = {"1", "2", "3"};
    private static final String LOCAL_AUTHORITY = "example";
    private static final String INVALID_LOCAL_AUTHORITY = "invalid local authority";
    private static final Set<String> caseRoles = Set.of("[LASOLICITOR]", "[CREATOR]");

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CaseUserApi caseUserApi;

    @MockBean
    private CaseAccessApi caseAccessApi;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private IdamClient client;

    @Autowired
    private SystemUpdateUserConfiguration userConfig;

    private LocalAuthorityUserService localAuthorityUserService;

    @BeforeEach
    void setup() {
        this.localAuthorityUserService = new LocalAuthorityUserService(
            caseAccessApi, organisationService,
            authTokenGenerator, caseUserApi, client, userConfig);

        given(client.authenticateUser(userConfig.getUserName(), userConfig.getPassword())).willReturn(AUTH_TOKEN);

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

        given(organisationService.findUserIdsInSameOrganisation(AUTH_TOKEN, USER_ID, LOCAL_AUTHORITY)).willReturn(
            ImmutableList.<String>builder()
                .add(USER_IDS)
                .build()
        );
    }

    @Test
    void shouldMakeCallToUpdateCaseRoleEndpointWhenUsersWithinLocalAuthority() {
        localAuthorityUserService.grantUserAccessWithCaseRole(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verifyUpdateCaseRolesWasCalledThisManyTimesForEachUser(1);
    }

    @Test
    void shouldThrowCustomExceptionWhenValidLocalAuthorityHasNoUsers() throws IllegalArgumentException {
        given(organisationService.findUserIdsInSameOrganisation(AUTH_TOKEN, USER_ID, LOCAL_AUTHORITY)).willReturn(
            ImmutableList.<String>builder().build()
        );

        assertThatThrownBy(() ->
            localAuthorityUserService.grantUserAccessWithCaseRole(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY))
            .isInstanceOf(NoAssociatedUsersException.class)
            .hasMessage("No users found for the local authority 'example'");
    }

    @Test
    void shouldThrowCustomExceptionWhenInValidLocalAuthorityHasNoUsers() throws IllegalArgumentException {
        assertThatThrownBy(() ->
            localAuthorityUserService
                .grantUserAccessWithCaseRole(AUTH_TOKEN, USER_ID, CASE_ID, INVALID_LOCAL_AUTHORITY))
            .isInstanceOf(NoAssociatedUsersException.class)
            .hasMessage("No users found for the local authority '" + INVALID_LOCAL_AUTHORITY + "'");
    }

    @Test
    void shouldNotThrowExceptionWhenCallToUpdateCaseRoleEndpointFailsForOneUser() {
        willThrow(new RetryableException(500, "Some error", null, null)).given(caseUserApi).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq("1"), refEq(new CaseUser("1", caseRoles)));

        localAuthorityUserService.grantUserAccessWithCaseRole(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verifyUpdateCaseRolesWasCalledThisManyTimesForEachUser(1);
    }

    @Test
    void shouldUpdateCaseRolesWhenRolesAreAlreadyAssignedToUser() {
        localAuthorityUserService.grantUserAccessWithCaseRole(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY);
        localAuthorityUserService.grantUserAccessWithCaseRole(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verifyUpdateCaseRolesWasCalledThisManyTimesForEachUser(2);
    }

    private void verifyUpdateCaseRolesWasCalledThisManyTimesForEachUser(int times) {
        verify(caseUserApi, times(times)).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[0]),
            refEq(new CaseUser(USER_IDS[0], caseRoles)));
        verify(caseUserApi, times(times)).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[1]),
            refEq(new CaseUser(USER_IDS[1], caseRoles)));
        verify(caseUserApi, times(times)).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(USER_IDS[2]),
            refEq(new CaseUser(USER_IDS[2], caseRoles)));
    }
}
