package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesResponse;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.exceptions.GrantCaseAccessException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.EPSMANAGING;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseAccessServiceTest {

    private static final Long CASE_ID = 0L;
    private static final String USER_1_ID = "1";
    private static final String USER_2_ID = "2";
    private static final String LOCAL_AUTHORITY = "LA1";
    private static final String AUTH_TOKEN = "User token";
    private static final String SERVICE_AUTH_TOKEN = "Service token";

    @Mock
    private IdamClient idam;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private CaseAccessDataStoreApi caseAccessDataStoreApi;

    @Spy
    private SystemUpdateUserConfiguration userConfig = new SystemUpdateUserConfiguration("SYS", "SYSPASS");

    @InjectMocks
    private CaseAccessService caseRoleService;

    @BeforeEach
    void setup() {
        given(idam.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).willReturn(AUTH_TOKEN);
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
    }

    @Nested
    class LocalAuthorityAccess {

        @Test
        void shouldGrantAccessToAllUsersWithOrganisationId() {
            final CaseRole caseRole = LASOLICITOR;

            final Set<String> localAuthorityUsers = new TreeSet<>();
            localAuthorityUsers.add(USER_1_ID);
            localAuthorityUsers.add(USER_2_ID);

            final Organisation organisation = Organisation.builder()
                .organisationIdentifier(randomAlphanumeric(5))
                .build();

            when(organisationService.findUserIdsInSameOrganisation(LOCAL_AUTHORITY)).thenReturn(localAuthorityUsers);
            when(organisationService.findOrganisation()).thenReturn(Optional.of(organisation));
            when(caseAccessDataStoreApi.addCaseUserRoles(any(), any(), any()))
                .thenReturn(AddCaseAssignedUserRolesResponse.builder().status("Granted").build());

            final AddCaseAssignedUserRolesRequest assignmentRequest = buildAssignmentRequest(CASE_ID,
                localAuthorityUsers, organisation.getOrganisationIdentifier(), caseRole);

            caseRoleService.grantCaseRoleToLocalAuthority(CASE_ID, LOCAL_AUTHORITY, caseRole);

            verify(caseAccessDataStoreApi).addCaseUserRoles(AUTH_TOKEN, SERVICE_AUTH_TOKEN, assignmentRequest);
        }

        @Test
        void shouldGrantAccessToAllUsersWithoutOrganisationId() {
            final CaseRole caseRole = LASOLICITOR;

            final Set<String> localAuthorityUsers = new TreeSet<>();
            localAuthorityUsers.add(USER_1_ID);
            localAuthorityUsers.add(USER_2_ID);

            when(organisationService.findUserIdsInSameOrganisation(LOCAL_AUTHORITY)).thenReturn(localAuthorityUsers);
            when(organisationService.findOrganisation()).thenReturn(Optional.empty());
            when(caseAccessDataStoreApi.addCaseUserRoles(any(), any(), any()))
                .thenReturn(AddCaseAssignedUserRolesResponse.builder().status("Granted").build());

            final AddCaseAssignedUserRolesRequest assignmentRequest =
                buildAssignmentRequest(CASE_ID, localAuthorityUsers, null, caseRole);

            caseRoleService.grantCaseRoleToLocalAuthority(CASE_ID, LOCAL_AUTHORITY, caseRole);

            verify(caseAccessDataStoreApi).addCaseUserRoles(AUTH_TOKEN, SERVICE_AUTH_TOKEN, assignmentRequest);
        }

        @Test
        void shouldThrowsExceptionWhenLocalAuthorityUsersCannotBeFetch() {
            Exception fetchUsersException = new RuntimeException(format("Can not fetch users for %s", LOCAL_AUTHORITY));

            doThrow(new RuntimeException()).when(organisationService).findUserIdsInSameOrganisation(LOCAL_AUTHORITY);

            final GrantCaseAccessException expectedException =
                new GrantCaseAccessException(CASE_ID, LOCAL_AUTHORITY, LASOLICITOR, fetchUsersException);

            final GrantCaseAccessException actualException = assertThrows(GrantCaseAccessException.class,
                () -> caseRoleService.grantCaseRoleToLocalAuthority(CASE_ID, LOCAL_AUTHORITY, LASOLICITOR));

            assertThat(actualException).isEqualTo(expectedException);
        }
    }

    @Nested
    class UserAccess {

        @Test
        void shouldGrantAccessToUser() {
            final CaseRole caseRole = EPSMANAGING;

            when(caseAccessDataStoreApi.addCaseUserRoles(any(), any(), any()))
                .thenReturn(AddCaseAssignedUserRolesResponse.builder().status("Granted").build());

            final AddCaseAssignedUserRolesRequest assignmentRequest =
                buildAssignmentRequest(CASE_ID, Set.of(USER_1_ID), null, caseRole);

            caseRoleService.grantCaseRoleToUser(CASE_ID, USER_1_ID, caseRole);

            verify(caseAccessDataStoreApi).addCaseUserRoles(AUTH_TOKEN, SERVICE_AUTH_TOKEN, assignmentRequest);
        }

        @Test
        void shouldRevokeCaseRoleFromUser() {
            caseRoleService.revokeCaseRoleFromUser(CASE_ID, USER_1_ID, CREATOR);

            CaseAssignedUserRolesRequest caseAssignedUserRolesRequest = CaseAssignedUserRolesRequest.builder()
                .caseAssignedUserRoles(List.of(CaseAssignedUserRoleWithOrganisation.builder()
                    .userId(USER_1_ID)
                    .caseRole(CREATOR.formattedName())
                    .caseDataId(CASE_ID.toString())
                    .build()))
                .build();

            verify(caseAccessDataStoreApi)
                .removeCaseUserRoles(AUTH_TOKEN, SERVICE_AUTH_TOKEN, caseAssignedUserRolesRequest);
        }
    }


    @Nested
    class UsersAccess {

        @Test
        void shouldGrantAccessToUsers() {
            final CaseRole caseRole = EPSMANAGING;

            Set<String> userIds = Set.of(USER_1_ID, USER_2_ID);

            when(caseAccessDataStoreApi.addCaseUserRoles(any(), any(), any()))
                .thenReturn(AddCaseAssignedUserRolesResponse.builder().status("Granted").build());

            final AddCaseAssignedUserRolesRequest assignmentRequest =
                buildAssignmentRequest(CASE_ID, userIds, null, caseRole);


            caseRoleService.grantCaseRoleToUsers(CASE_ID, userIds, caseRole);

            verify(caseAccessDataStoreApi).addCaseUserRoles(AUTH_TOKEN, SERVICE_AUTH_TOKEN, assignmentRequest);
        }
    }

    private AddCaseAssignedUserRolesRequest buildAssignmentRequest(Long caseId, Set<String> userIds, String orgId,
                                                                   CaseRole caseRole) {
        final List<CaseAssignedUserRoleWithOrganisation> caseAssignedRoles = userIds.stream()
            .map(userId -> CaseAssignedUserRoleWithOrganisation.builder()
                .caseDataId(caseId.toString())
                .userId(userId)
                .organisationId(orgId)
                .caseRole(caseRole.formattedName())
                .build())
            .collect(Collectors.toList());

        return AddCaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(caseAssignedRoles)
            .build();
    }
}
