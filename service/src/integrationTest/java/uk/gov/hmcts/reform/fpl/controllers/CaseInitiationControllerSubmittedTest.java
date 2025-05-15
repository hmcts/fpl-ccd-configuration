package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.OutsourcingType;
import uk.gov.hmcts.reform.fpl.exceptions.GrantCaseAccessException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.hmcts.reform.rd.model.OrganisationUser;
import uk.gov.hmcts.reform.rd.model.OrganisationUsers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.Set.of;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feignException;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOrganisation;
import static uk.gov.hmcts.reform.rd.model.Status.ACTIVE;

@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerSubmittedTest extends AbstractCallbackTest {

    private static final String LOGGED_USER_ID = USER_ID;
    private static final String OTHER_USER_ID = randomUUID().toString();

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    CaseInitiationControllerSubmittedTest() {
        super("case-initiation");
    }

    @BeforeEach
    void setup() {
        givenSystemUser();
        givenFplService();
    }

    @Test
    void shouldGrantCaseAccessToAllUsersInOrganisation() {
        final Organisation organisation = testOrganisation();

        givenUserInOrganisation(organisation);
        givenUsersInSameOrganisation(LOGGED_USER_ID, OTHER_USER_ID);

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .state(OPEN)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .localAuthorityPolicy(
                organisationPolicy(organisation.getOrganisationIdentifier(), organisation.getName(), LASOLICITOR))
            .build();

        postSubmittedEvent(caseData);

        CaseAssignmentUserRolesRequest expectedUnAssignment = unAssignment(caseData, CREATOR, LOGGED_USER_ID);

        CaseAssignmentUserRolesRequest expectedAssignment = assignment(caseData, organisation, LASOLICITOR,
            LOGGED_USER_ID, OTHER_USER_ID);

        verify(caseAssignmentApi).addCaseUserRoles(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedAssignment);
        verify(caseAssignmentApi).removeCaseUserRoles(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedUnAssignment);

        verifyTaskListUpdated(caseData);

        verifyNoMoreInteractions(caseAssignmentApi);
    }

    @ParameterizedTest
    @EnumSource(OutsourcingType.class)
    void shouldGrantCaseAccessToOutsourcedUserOnly(OutsourcingType outsourcingType) {
        final Organisation organisation = testOrganisation();
        final CaseRole outsourcedCaseRole = outsourcingType.getCaseRole();

        givenUserInOrganisation(organisation);
        givenUsersInSameOrganisation(LOGGED_USER_ID, OTHER_USER_ID);

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .state(OPEN)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .outsourcingPolicy(organisationPolicy(
                organisation.getOrganisationIdentifier(), organisation.getName(), outsourcedCaseRole))
            .build();

        postSubmittedEvent(caseData);

        CaseAssignmentUserRolesRequest expectedUnAssignment = unAssignment(caseData, CREATOR, LOGGED_USER_ID);

        CaseAssignmentUserRolesRequest expectedUserAssignment = assignment(caseData, organisation, outsourcedCaseRole,
            LOGGED_USER_ID);

        verify(caseAssignmentApi).removeCaseUserRoles(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedUnAssignment);
        verify(caseAssignmentApi).addCaseUserRoles(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedUserAssignment);

        verifyNoMoreInteractions(caseAssignmentApi);

        verifyTaskListUpdated(caseData);
    }

    @Test
    void shouldThrowExceptionWhenAccessNotGranted() {
        final Organisation organisation = testOrganisation();

        givenUsersInSameOrganisation(LOGGED_USER_ID, OTHER_USER_ID);

        doThrow(feignException(SC_BAD_REQUEST)).when(caseAssignmentApi).addCaseUserRoles(any(), any(), any());

        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .localAuthorityPolicy(
                organisationPolicy(organisation.getOrganisationIdentifier(), organisation.getName(), LASOLICITOR))
            .build();

        // Exception is only thrown now at the very end if the creator also couldn't be assigned the role
        assertThatThrownBy(() -> postSubmittedEvent(caseData))
            .getRootCause()
            .isEqualTo(new GrantCaseAccessException(caseData.getId(), of(LOGGED_USER_ID), LASOLICITOR));
    }

    private void givenUserInOrganisation(Organisation organisation) {
        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(organisation);
    }

    private void givenUsersInSameOrganisation(String... userIds) {
        given(organisationApi.findUsersInCurrentUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, ACTIVE, false))
            .willReturn(organisation(userIds));
    }

    private void verifyTaskListUpdated(CaseData caseData) {
        verify(coreCaseDataService).performPostSubmitCallback(
            eq(caseData.getId()),
            eq("internal-update-task-list"),
            any());
    }

    private CaseAssignmentUserRolesRequest assignment(CaseData caseData, Organisation organisation,
                                                       CaseRole caseRole, String... users) {

        List<CaseAssignmentUserRoleWithOrganisation> assignments = Stream.of(users)
            .map(userId -> CaseAssignmentUserRoleWithOrganisation.builder()
                .organisationId(ofNullable(organisation).map(Organisation::getOrganisationIdentifier).orElse(null))
                .caseRole(caseRole.formattedName())
                .caseDataId(caseData.getId().toString())
                .userId(userId)
                .build())
            .collect(Collectors.toList());

        return CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(assignments)
            .build();
    }

    private CaseAssignmentUserRolesRequest unAssignment(CaseData caseData, CaseRole caseRole, String userId) {
        return CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(List.of(
                CaseAssignmentUserRoleWithOrganisation.builder()
                    .caseRole(caseRole.formattedName())
                    .caseDataId(caseData.getId().toString())
                    .userId(userId)
                    .build()))
            .build();
    }

    private static OrganisationUsers organisation(String... userIds) {
        List<OrganisationUser> users = Stream.of(userIds)
            .map(id -> OrganisationUser.builder().userIdentifier(id).build())
            .collect(toList());

        return OrganisationUsers.builder().users(users).build();
    }

    @Test
    void shouldInvokeSubmitSupplementaryData() {
        final Organisation organisation = testOrganisation();

        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .state(OPEN)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .outsourcingPolicy(organisationPolicy(
                organisation.getOrganisationIdentifier(), organisation.getName(), LASOLICITOR))
            .build();

        postSubmittedEvent(caseData);

        Map<String, Map<String, Map<String, Object>>> supplementaryData = new HashMap<>();
        supplementaryData.put("supplementary_data_updates",
            Map.of("$set", Map.of("HMCTSServiceId", "ABA3")));

        verify(coreCaseDataApi).submitSupplementaryData(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN,
            caseData.getId().toString(), supplementaryData);
    }
}
