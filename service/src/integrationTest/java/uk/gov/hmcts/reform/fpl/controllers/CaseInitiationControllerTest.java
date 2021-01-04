package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesResponse;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRoleWithOrganisation;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.GrantCaseAccessException;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityCodeException;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.hmcts.reform.rd.model.OrganisationUser;
import uk.gov.hmcts.reform.rd.model.OrganisationUsers;
import uk.gov.hmcts.reform.rd.model.Status;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feignException;
import static uk.gov.hmcts.reform.fpl.utils.assertions.ExceptionAssertion.assertException;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseInitiationController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseInitiationControllerTest extends AbstractControllerTest {

    private static final String CALLER_ID = USER_ID;

    private static final String LA_NOT_IN_PRD_CODE = "LA_1";
    private static final String LA_NOT_IN_PRD_USER_1_ID = "LA_1-1";
    private static final String LA_NOT_IN_PRD_USER_2_ID = "LA_1-2";
    private static final List<String> LA_NOT_IN_PRD_USER_IDS = List.of(
        CALLER_ID, LA_NOT_IN_PRD_USER_1_ID, LA_NOT_IN_PRD_USER_2_ID
    );

    private static final String LA_IN_PRD_CODE = "LA_2";
    private static final String LA_IN_PRD_USER_1_ID = "LA_2-1";
    private static final String LA_IN_PRD_USER_2_ID = "LA_2-2";
    private static final List<String> LA_IN_PRD_USER_IDS = List.of(CALLER_ID, LA_IN_PRD_USER_1_ID, LA_IN_PRD_USER_2_ID);

    private static final String CASE_ID = "12345";

    @MockBean
    private LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration;

    @MockBean
    private OrganisationApi organisationApi;

    @MockBean
    private ServiceAuthorisationApi serviceAuthorisationApi;

    @MockBean
    private CaseUserApi caseUserApi;

    @MockBean
    private CaseAccessDataStoreApi caseDataAccessApi;

    @MockBean
    private IdamClient client;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Autowired
    private SystemUpdateUserConfiguration userConfig;

    CaseInitiationControllerTest() {
        super("case-initiation");
    }

    @BeforeEach
    void setup() {
        given(client.getAccessToken(userConfig.getUserName(), userConfig.getPassword())).willReturn(USER_AUTH_TOKEN);

        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

        given(serviceAuthorisationApi.serviceToken(anyMap())).willReturn(SERVICE_AUTH_TOKEN);

        given(client.getUserInfo(USER_AUTH_TOKEN)).willReturn(
            UserInfo.builder().sub("user@example.gov.uk").build());

        given(localAuthorityUserLookupConfiguration.getUserIds(LA_NOT_IN_PRD_CODE)).willReturn(LA_NOT_IN_PRD_USER_IDS);

        given(localAuthorityUserLookupConfiguration.getUserIds(LA_IN_PRD_CODE))
            .willThrow(new UnknownLocalAuthorityCodeException(LA_IN_PRD_CODE));
    }

    @Test
    void shouldAddCaseLocalAuthorityAndOrganisationPolicy() {
        Organisation organisation = Organisation.builder()
            .name(randomAlphanumeric(5))
            .organisationIdentifier(UUID.randomUUID().toString())
            .build();

        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(organisation);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseName", "title"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData())
            .containsEntry("caseName", "title")
            .containsEntry("caseLocalAuthority", "example")
            .containsEntry("localAuthorityPolicy", Map.of(
                "Organisation", Map.of(
                    "OrganisationID", organisation.getOrganisationIdentifier()),
                "OrgPolicyCaseAssignedRole", "[LASOLICITOR]"));
    }

    @Test
    void shouldSkipOrganisationPolicyIfUserNotRegisteredInOrganisation() {
        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN))
            .willThrow(feignException(SC_FORBIDDEN));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseName", "title"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData())
            .containsEntry("caseName", "title")
            .containsEntry("caseLocalAuthority", "example");
    }

    @Test
    void shouldPopulateErrorsInResponseWhenDomainNameIsNotFound() {
        AboutToStartOrSubmitCallbackResponse expectedResponse = AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of("The email address was not linked to a known Local Authority"))
            .build();

        given(client.getUserInfo(USER_AUTH_TOKEN))
            .willReturn(UserInfo.builder().sub("user@email.gov.uk").build());

        AboutToStartOrSubmitCallbackResponse actualResponse = postAboutToSubmitEvent(
            "core-case-data-store-api/empty-case-details.json");

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void shouldGrantCaseAssignmentAccessToListOfUsers() {
        final Organisation organisation = Organisation.builder()
            .organisationIdentifier(RandomStringUtils.randomAlphanumeric(5))
            .build();

        given(caseDataAccessApi.addCaseUserRoles(anyString(), anyString(),
            any(AddCaseAssignedUserRolesRequest.class)))
            .willReturn(AddCaseAssignedUserRolesResponse.builder().build());
        givenPRDWillReturn(LA_IN_PRD_USER_IDS);
        given(caseDataAccessApi.addCaseUserRoles(any(), any(), any()))
            .willReturn(AddCaseAssignedUserRolesResponse.builder().status("").build());
        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(organisation);

        final CallbackRequest callbackRequest = getCase(LA_IN_PRD_CODE);

        postSubmittedEvent(callbackRequest);

        AddCaseAssignedUserRolesRequest expectedUserAssignment = AddCaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(List.of(
                CaseAssignedUserRoleWithOrganisation.builder()
                    .caseRole(LASOLICITOR.formattedName())
                    .caseDataId(callbackRequest.getCaseDetails().getId().toString())
                    .userId(CALLER_ID)
                    .organisationId(organisation.getOrganisationIdentifier())
                    .build(),
                CaseAssignedUserRoleWithOrganisation.builder()
                    .caseRole(LASOLICITOR.formattedName())
                    .caseDataId(callbackRequest.getCaseDetails().getId().toString())
                    .userId(LA_IN_PRD_USER_1_ID)
                    .organisationId(organisation.getOrganisationIdentifier())
                    .build(),
                CaseAssignedUserRoleWithOrganisation.builder()
                    .caseRole(LASOLICITOR.formattedName())
                    .caseDataId(callbackRequest.getCaseDetails().getId().toString())
                    .userId(LA_IN_PRD_USER_2_ID)
                    .organisationId(organisation.getOrganisationIdentifier())
                    .build()
            ))
            .build();

        verify(caseDataAccessApi).addCaseUserRoles(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedUserAssignment);
        verifyNoInteractions(caseUserApi);
    }

    @Test
    void shouldThrowExceptionWhenAccessNotGranted() {
        given(caseDataAccessApi.addCaseUserRoles(anyString(), anyString(),
            any(AddCaseAssignedUserRolesRequest.class)))
            .willReturn(AddCaseAssignedUserRolesResponse.builder().build());
        doThrow(TestDataHelper.feignException(400))
            .when(caseDataAccessApi).addCaseUserRoles(any(), any(), any(AddCaseAssignedUserRolesRequest.class));
        givenPRDWillReturn(LA_NOT_IN_PRD_USER_IDS);
        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(null);

        CallbackRequest callbackRequest = getCase(LA_NOT_IN_PRD_CODE);
        final Exception exception = assertThrows(Exception.class,
            () -> postSubmittedEvent(callbackRequest));

        assertException(exception)
            .isCausedBy(new GrantCaseAccessException(CASE_ID, new HashSet<>(LA_NOT_IN_PRD_USER_IDS),
                Set.of(CREATOR, LASOLICITOR)));

        AddCaseAssignedUserRolesRequest expectedUserAssignment = AddCaseAssignedUserRolesRequest.builder()
            .caseAssignedUserRoles(List.of(
                getCaseAssignedRoleForUser(callbackRequest.getCaseDetails().getId().toString(), CALLER_ID),
                getCaseAssignedRoleForUser(callbackRequest.getCaseDetails().getId().toString(),
                    LA_NOT_IN_PRD_USER_1_ID),
                getCaseAssignedRoleForUser(callbackRequest.getCaseDetails().getId().toString(),
                    LA_NOT_IN_PRD_USER_2_ID)))
            .build();

        verify(caseDataAccessApi).addCaseUserRoles(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedUserAssignment);
        verifyNoInteractions(caseUserApi);
    }

    @Test
    void shouldPopulateTaskListOnSuccessfulSubmission() {
        final Organisation organisation = Organisation.builder()
            .organisationIdentifier(RandomStringUtils.randomAlphanumeric(5))
            .build();

        given(caseDataAccessApi.addCaseUserRoles(anyString(), anyString(),
            any(AddCaseAssignedUserRolesRequest.class)))
            .willReturn(AddCaseAssignedUserRolesResponse.builder().build());
        givenPRDWillReturn(LA_IN_PRD_USER_IDS);
        given(caseDataAccessApi.addCaseUserRoles(any(), any(), any()))
            .willReturn(AddCaseAssignedUserRolesResponse.builder().status("").build());
        given(organisationApi.findUserOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN)).willReturn(organisation);

        final CallbackRequest callbackRequest = getCase(LA_IN_PRD_CODE);

        postSubmittedEvent(callbackRequest);

        verifyTaskListUpdated(callbackRequest.getCaseDetails());
    }

    private CaseAssignedUserRoleWithOrganisation getCaseAssignedRoleForUser(String caseId, String userId) {
        return CaseAssignedUserRoleWithOrganisation.builder()
            .caseRole(LASOLICITOR.formattedName())
            .caseDataId(caseId)
            .userId(userId)
            .build();
    }

    private static OrganisationUsers organisation(List<String> userIds) {
        List<OrganisationUser> users = userIds.stream()
            .map(id -> OrganisationUser.builder().userIdentifier(id).build())
            .collect(toList());

        return OrganisationUsers.builder().users(users).build();
    }

    private static CallbackRequest getCase(String localAuthority) {
        return callbackRequest(Map.of("localAuthority", localAuthority));
    }

    private void verifyTaskListUpdated(CaseDetails caseDetails) {
        verify(coreCaseDataService).triggerEvent(
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(caseDetails.getId()),
            eq("internal-update-task-list"),
            anyMap());
    }

    private void givenPRDWillReturn(List<String> userIds) {
        given(organisationApi.findUsersInOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN,
                                                    Status.ACTIVE, false)).willReturn(organisation(userIds));
    }
}
