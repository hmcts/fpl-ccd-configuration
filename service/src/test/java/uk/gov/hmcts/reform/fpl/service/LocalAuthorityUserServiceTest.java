package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import feign.FeignException;
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
import uk.gov.hmcts.reform.fpl.exceptions.UserOrganisationLookupException;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, SystemUpdateUserConfiguration.class})
class LocalAuthorityUserServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "3";
    private static final String USER_NOT_IN_LA_ID = "4";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";
    private static final String CASE_ID = "1";
    private static final List<String> USER_IDS = List.of("1", "2", "3");
    private static final String LOCAL_AUTHORITY = "example";
    private static final Set<String> caseRoles = Set.of("[LASOLICITOR]", "[CREATOR]");

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CaseUserApi caseUserApi;

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
            organisationService,
            authTokenGenerator, caseUserApi, client, userConfig);

        given(client.authenticateUser(userConfig.getUserName(), userConfig.getPassword())).willReturn(AUTH_TOKEN);

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

        given(organisationService.findUserIdsInSameOrganisation(AUTH_TOKEN, LOCAL_AUTHORITY)).willReturn(
            ImmutableList.<String>builder()
                .addAll(USER_IDS)
                .build()
        );
    }

    @Test
    void shouldMakeCallToUpdateCaseRoleEndpointWhenUsersWithinLocalAuthority() {
        localAuthorityUserService.grantUserAccessWithCaseRole(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verifyUpdateCaseRolesWasCalledThisManyTimesForEachUser(1, USER_IDS);
    }

    @Test
    void shouldAddCallerUserIdToACaseEvenIfNotPartOfLocalAuthority()  {
        localAuthorityUserService.grantUserAccessWithCaseRole(AUTH_TOKEN, USER_NOT_IN_LA_ID, CASE_ID, LOCAL_AUTHORITY);

        List<String> userIdsIncludingCallerId = ImmutableList
            .<String>builder()
            .addAll(USER_IDS)
            .add(USER_NOT_IN_LA_ID)
            .build();

        verifyUpdateCaseRolesWasCalledThisManyTimesForEachUser(1, userIdsIncludingCallerId);
    }

    @Test
    void shouldAddCallerUserIdToACaseWhenValidLocalAuthorityHasNoUsers()  {
        given(organisationService.findUserIdsInSameOrganisation(AUTH_TOKEN, LOCAL_AUTHORITY)).willReturn(
            ImmutableList.<String>builder().build()
        );

        localAuthorityUserService.grantUserAccessWithCaseRole(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verifyUpdateCaseRolesWasCalledThisManyTimesForEachUser(1, List.of(USER_ID));
    }

    @Test
    void shouldAddCallerUserIdToACaseWhenServiceThrowsAnException()  {
        given(organisationService.findUserIdsInSameOrganisation(any(), any()))
            .willThrow(new NullPointerException());

        localAuthorityUserService.grantUserAccessWithCaseRole(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verifyUpdateCaseRolesWasCalledThisManyTimesForEachUser(1, List.of(USER_ID));
    }

    @Test
    void shouldNotThrowExceptionWhenCallToUpdateCaseRoleEndpointFailsForOneUser() {
        willThrow(new RetryableException(500, "Some error", null, null)).given(caseUserApi).updateCaseRolesForUser(
            eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq("1"), refEq(new CaseUser("1", caseRoles)));

        localAuthorityUserService.grantUserAccessWithCaseRole(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verifyUpdateCaseRolesWasCalledThisManyTimesForEachUser(1, USER_IDS);
    }

    @Test
    void shouldUpdateCaseRolesWhenRolesAreAlreadyAssignedToUser() {
        localAuthorityUserService.grantUserAccessWithCaseRole(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY);
        localAuthorityUserService.grantUserAccessWithCaseRole(AUTH_TOKEN, USER_ID, CASE_ID, LOCAL_AUTHORITY);

        verifyUpdateCaseRolesWasCalledThisManyTimesForEachUser(2, USER_IDS);
    }


    private void verifyUpdateCaseRolesWasCalledThisManyTimesForEachUser(int times, List<String> userIds) {
        for (String userId : userIds) {
            verify(caseUserApi, times(times)).updateCaseRolesForUser(
                eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(CASE_ID), eq(userId),
                refEq(new CaseUser(userId, caseRoles)));
        }
    }
}
