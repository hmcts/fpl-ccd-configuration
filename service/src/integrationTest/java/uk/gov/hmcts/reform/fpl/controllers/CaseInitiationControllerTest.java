package uk.gov.hmcts.reform.fpl.controllers;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.stubbing.Answer;
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
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.ccd.model.AddCaseAssignedUserRolesResponse;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.GrantCaseAccessException;
import uk.gov.hmcts.reform.fpl.exceptions.UnknownLocalAuthorityCodeException;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.hmcts.reform.rd.model.OrganisationUser;
import uk.gov.hmcts.reform.rd.model.OrganisationUsers;
import uk.gov.hmcts.reform.rd.model.Status;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
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
    private static final List<String> LA_NOT_IN_PRD_USER_IDS =
        List.of(CALLER_ID, LA_NOT_IN_PRD_USER_1_ID, LA_NOT_IN_PRD_USER_2_ID);

    private static final String LA_IN_PRD_CODE = "LA_2";
    private static final String LA_IN_PRD_USER_1_ID = "LA_2-1";
    private static final String LA_IN_PRD_USER_2_ID = "LA_2-2";
    private static final List<String> LA_IN_PRD_USER_IDS = List.of(CALLER_ID, LA_IN_PRD_USER_1_ID, LA_IN_PRD_USER_2_ID);

    private static final String CASE_ID = "12345";
    private static final Set<String> CASE_ROLES = Set.of("[LASOLICITOR]", "[CREATOR]");
    private static final String PAGE_SHOW = "pageShow";

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

    @Autowired
    private FeatureToggleService featureToggleService;

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

        given(caseDataAccessApi.addCaseUserRoles(anyString(), anyString(), any(AddCaseAssignedUserRolesRequest.class)))
            .willReturn(AddCaseAssignedUserRolesResponse.builder().build());
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
                    "OrganisationID", organisation.getOrganisationIdentifier(),
                    "OrganisationName", organisation.getName()),
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
    void updateCaseRolesShouldBeCalledOnceForEachUserFetchedFromPRD() {
        if (!featureToggleService.isCaseUserAssignmentEnabled()) {
            givenPRDWillReturn(LA_IN_PRD_USER_IDS);

            final CallbackRequest request = getCase(LA_IN_PRD_CODE);

            postSubmittedEvent(request);

            verifyGrantCaseRoleAttempts(LA_IN_PRD_USER_IDS);
            verifyTaskListUpdated(request.getCaseDetails());
        }
    }

    @Test
    void updateCaseRolesShouldBeCalledForEachFromCustomUserMappingIfOrganisationNotInPRD() {
        if (!featureToggleService.isCaseUserAssignmentEnabled()) {
            givenPRDWillFail();

            final CallbackRequest request = getCase(LA_NOT_IN_PRD_CODE);

            postSubmittedEvent(request);

            verifyGrantCaseRoleAttempts(LA_NOT_IN_PRD_USER_IDS);
            verifyTaskListUpdated(request.getCaseDetails());
        }
    }

    @Test
    void updateCaseRolesShouldBeCalledOnlyForCaseCreatorIfOrganisationNotInPRDAndNoCustomUserMapping() {
        if (!featureToggleService.isCaseUserAssignmentEnabled()) {
            givenPRDWillFail();

            final CallbackRequest request = getCase(LA_IN_PRD_CODE);

            postSubmittedEvent(request);

            verifyUsersFetchFromPrd(3);
            verifyGrantCaseRoleAttempts(List.of(CALLER_ID));
            verifyTaskListUpdated(request.getCaseDetails());
        }
    }

    @Test
    void shouldRetryPRDCallOnFailure() {
        if (!featureToggleService.isCaseUserAssignmentEnabled()) {
            givenPRDWillAnswer(
                invocation -> {
                    throw new RuntimeException();
                },
                invocation -> organisation(LA_IN_PRD_USER_IDS));

            final CallbackRequest request = getCase(LA_IN_PRD_CODE);

            postSubmittedEvent(request);

            verifyUsersFetchFromPrd(2);
            verifyGrantCaseRoleAttempts(LA_IN_PRD_USER_IDS);
            verifyTaskListUpdated(request.getCaseDetails());
        }
    }

    @Test
    void shouldGrantCaseAccessToOtherUsersAndThrowExceptionWhenCallerAccessNotGranted() {
        if (!featureToggleService.isCaseUserAssignmentEnabled()) {
            doThrow(RuntimeException.class)
                .when(caseUserApi).updateCaseRolesForUser(any(), any(), any(), eq(CALLER_ID), any());

            givenPRDWillReturn(LA_NOT_IN_PRD_USER_IDS);

            final Exception exception = assertThrows(Exception.class,
                () -> postSubmittedEvent(getCase(LA_NOT_IN_PRD_CODE)));

            assertException(exception)
                .isCausedBy(new GrantCaseAccessException(CASE_ID, Set.of(USER_ID), Set.of(CREATOR, LASOLICITOR)));

            verifyGrantCaseRoleAttempts(LA_NOT_IN_PRD_USER_IDS);
        }
    }

    @Test
    void shouldAttemptGrantAccessToAllLocalAuthorityUsersWhenGrantAccessFailsForSomeOfThem() {
        if (!featureToggleService.isCaseUserAssignmentEnabled()) {
            doThrow(RuntimeException.class)
                .doNothing()
                .when(caseUserApi).updateCaseRolesForUser(any(), any(), any(), eq(LA_NOT_IN_PRD_USER_1_ID), any());

            givenPRDWillReturn(LA_NOT_IN_PRD_USER_IDS);

            postSubmittedEvent(getCase(LA_NOT_IN_PRD_CODE));

            verifyGrantCaseRoleAttempts(LA_NOT_IN_PRD_USER_IDS, 2);
        }
    }

    @Test
    void shouldGrantCaseAssignmentAccessToListOfUsers() {
        if (featureToggleService.isCaseUserAssignmentEnabled()) {
            AddCaseAssignedUserRolesResponse response = new AddCaseAssignedUserRolesResponse();
            response.setStatus("");
            given(caseDataAccessApi.addCaseUserRoles(any(), any(), any())).willReturn(response);

            postSubmittedEvent(getCase(LA_NOT_IN_PRD_CODE));

            AddCaseAssignedUserRolesRequest request = new AddCaseAssignedUserRolesRequest();
            request.setCaseAssignedUserRoles(emptyList());

            verify(caseDataAccessApi).addCaseUserRoles(
                USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN,request);
        }
    }

    @Test
    void shouldThrowExceptionWhenCallerAccessNotGranted() {
        if (featureToggleService.isCaseUserAssignmentEnabled()) {
            doThrow(RuntimeException.class)
                .when(caseDataAccessApi).addCaseUserRoles(any(), any(), new AddCaseAssignedUserRolesRequest());

            givenPRDWillReturn(LA_NOT_IN_PRD_USER_IDS);

            final Exception exception = assertThrows(Exception.class,
                () -> postSubmittedEvent(getCase(LA_NOT_IN_PRD_CODE)));

            assertException(exception)
                .isCausedBy(new GrantCaseAccessException(CASE_ID, Set.of(USER_ID), Set.of(CREATOR, LASOLICITOR)));

            AddCaseAssignedUserRolesRequest request = new AddCaseAssignedUserRolesRequest();
            request.setCaseAssignedUserRoles(emptyList());

            verify(caseDataAccessApi).addCaseUserRoles(
                USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN,request);
        }
    }

    private void verifyGrantCaseRoleAttempts(List<String> users, int attempts) {
        verify(caseUserApi).updateCaseRolesForUser(
            USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID, CALLER_ID,
            new CaseUser(CALLER_ID, CASE_ROLES));

        checkUntil(() -> users.stream()
            .filter(userId -> !CALLER_ID.equals(userId))
            .forEach(userId ->
                verify(caseUserApi, times(attempts)).updateCaseRolesForUser(
                    USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, CASE_ID, userId,
                    new CaseUser(userId, CASE_ROLES))));
    }

    private void verifyGrantCaseRoleAttempts(List<String> users) {
        verifyGrantCaseRoleAttempts(users, 1);
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

    private void givenPRDWillFail() {
        Request request = Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null);
        givenPRDWillAnswer(invocation -> {
            throw new FeignException.NotFound("", request, new byte[]{});
        });
    }

    private void givenPRDWillReturn(List<String> userIds) {
        givenPRDWillAnswer(invocation -> organisation(userIds));
    }

    private void givenPRDWillAnswer(Answer... answers) {
        BDDMockito.BDDMyOngoingStubbing<OrganisationUsers> stub = given(organisationApi.findUsersInOrganisation(
            USER_AUTH_TOKEN,
            SERVICE_AUTH_TOKEN,
            Status.ACTIVE,
            false));
        for (Answer answer : answers) {
            stub = stub.willAnswer(answer);
        }
    }

    private void verifyUsersFetchFromPrd(int times) {
        checkUntil(() -> verify(organisationApi, times(times))
            .findUsersInOrganisation(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, Status.ACTIVE, false));
    }
}
