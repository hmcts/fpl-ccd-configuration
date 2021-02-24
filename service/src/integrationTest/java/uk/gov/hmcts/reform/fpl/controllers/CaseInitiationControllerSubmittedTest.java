package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.exceptions.GrantCaseAccessException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.hmcts.reform.rd.model.OrganisationUser;
import uk.gov.hmcts.reform.rd.model.OrganisationUsers;
import uk.gov.hmcts.reform.rd.model.Status;

import java.util.List;
import java.util.stream.Stream;

import static java.util.List.of;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_CODE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.EPSMANAGING;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feignException;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOrganisation;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerSubmittedTest extends AbstractControllerTest {

    private static final String LOGGED_USER_ID = USER_ID;
    private static final String OTHER_USER_ID = randomUUID().toString();

    @MockBean
    private IdamClient client;

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private SystemUpdateUserConfiguration userConfig;

    @MockBean
    private CaseAccessDataStoreApi caseDataAccessApi;

    CaseInitiationControllerSubmittedTest() {
        super("case-initiation");
    }

    @BeforeEach
    void setup() {
        given(client.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).willReturn(USER_AUTH_TOKEN);
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldGrantCaseAccessToAllUsersInOrganisation() {
        final Organisation organisation = testOrganisation();

        givenUserInOrganisation(organisation);
        givenUsersInSameOrganisation(LOGGED_USER_ID, OTHER_USER_ID);

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .state(State.OPEN)
            .caseLocalAuthority(DEFAULT_LA_CODE)
            .build();

        postSubmittedEvent(caseData);

        AddCaseAssignedUserRolesRequest expectedUserAssignment = AddCaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(List.of(
                assignment(caseData.getId(), organisation, LASOLICITOR, LOGGED_USER_ID),
                assignment(caseData.getId(), organisation, LASOLICITOR, OTHER_USER_ID)))
            .build();

        verify(caseDataAccessApi).addCaseUserRoles(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedUserAssignment);

        verifyTaskListUpdated(caseData);

        verifyNoMoreInteractions(caseDataAccessApi);
    }

    @Test
    void shouldGrantCaseAccessToOutsourceUserOnly() {
        final Organisation organisation = testOrganisation();

        givenUserInOrganisation(organisation);
        givenUsersInSameOrganisation(LOGGED_USER_ID, OTHER_USER_ID);

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .state(State.OPEN)
            .caseLocalAuthority(DEFAULT_LA_CODE)
            .outsourcingPolicy(organisationPolicy(organisation.getOrganisationIdentifier(), EPSMANAGING))
            .build();

        postSubmittedEvent(caseData);

        final CaseAssignedUserRolesRequest expectedUserUnAssignment = CaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(List.of(assignment(caseData.getId(), CREATOR, LOGGED_USER_ID)))
            .build();

        final AddCaseAssignedUserRolesRequest expectedUserAssignment = AddCaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(List.of(assignment(caseData.getId(), organisation, EPSMANAGING, LOGGED_USER_ID)))
            .build();

        verify(caseDataAccessApi).removeCaseUserRoles(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedUserUnAssignment);
        verify(caseDataAccessApi).addCaseUserRoles(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedUserAssignment);

        verifyNoMoreInteractions(caseDataAccessApi);

        verifyTaskListUpdated(caseData);
    }

    @Test
    void shouldThrowExceptionWhenAccessNotGranted() {
        givenUsersInSameOrganisation(LOGGED_USER_ID, OTHER_USER_ID);

        doThrow(feignException(SC_BAD_REQUEST)).when(caseDataAccessApi).addCaseUserRoles(any(), any(), any());

        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .caseLocalAuthority(DEFAULT_LA_CODE)
            .build();

        assertThatThrownBy(() -> postSubmittedEvent(caseData))
            .getRootCause()
            .isEqualTo(new GrantCaseAccessException(caseData.getId(), of(LOGGED_USER_ID, OTHER_USER_ID), LASOLICITOR));

        AddCaseAssignedUserRolesRequest expectedUserAssignment = AddCaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(List.of(
                assignment(caseData.getId(), LASOLICITOR, LOGGED_USER_ID),
                assignment(caseData.getId(), LASOLICITOR, OTHER_USER_ID)))
            .build();

        verify(caseDataAccessApi).addCaseUserRoles(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedUserAssignment);

        verifyNoMoreInteractions(caseDataAccessApi);
    }

    private void givenUserInOrganisation(Organisation organisation) {
        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(organisation);
    }

    private void givenUsersInSameOrganisation(String... userIds) {
        given(organisationApi.findUsersInOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, Status.ACTIVE, false))
            .willReturn(organisation(userIds));
    }

    private void verifyTaskListUpdated(CaseData caseData) {
        verify(coreCaseDataService).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(caseData.getId()),
            eq("internal-update-task-list"),
            anyMap());
    }

    private CaseAssignedUserRoleWithOrganisation assignment(Long caseId, Organisation organisation, CaseRole caseRole,
                                                            String userId) {
        return CaseAssignedUserRoleWithOrganisation.builder()
            .organisationId(ofNullable(organisation).map(Organisation::getOrganisationIdentifier).orElse(null))
            .caseRole(caseRole.formattedName())
            .caseDataId(caseId.toString())
            .userId(userId)
            .build();
    }

    private CaseAssignedUserRoleWithOrganisation assignment(Long caseId, CaseRole caseRole, String userId) {
        return assignment(caseId, null, caseRole, userId);
    }

    private static OrganisationUsers organisation(String... userIds) {
        List<OrganisationUser> users = Stream.of(userIds)
            .map(id -> OrganisationUser.builder().userIdentifier(id).build())
            .collect(toList());

        return OrganisationUsers.builder().users(users).build();
    }

}
