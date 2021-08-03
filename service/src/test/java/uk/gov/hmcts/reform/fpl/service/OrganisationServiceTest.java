package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.UserLookupException;
import uk.gov.hmcts.reform.fpl.exceptions.UserOrganisationLookupException;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.ContactInformation;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.hmcts.reform.rd.model.OrganisationUser;
import uk.gov.hmcts.reform.rd.model.OrganisationUsers;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_GATEWAY_TIMEOUT;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feignException;
import static uk.gov.hmcts.reform.rd.model.Status.ACTIVE;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceTest {

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private OrganisationApi organisationApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock(lenient = true)
    private RequestData requestData;

    @Spy
    private final LocalAuthorityUserLookupConfiguration lookupSpy = new LocalAuthorityUserLookupConfiguration(
        "SA=>1|2|3"
    );

    @InjectMocks
    private OrganisationService organisationService;

    private static final String AUTH_TOKEN = "Bearer authorisedBearer";
    private static final String SERVICE_AUTH_TOKEN = "Bearer authorised service";
    private static final String USER_EMAIL = "test@test.com";
    private static final Organisation POPULATED_ORGANISATION = buildOrganisation();

    @BeforeEach
    void setup() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(requestData.authorisation()).thenReturn(AUTH_TOKEN);
    }

    @Nested
    class FindUsersInOrganisation {

        @Test
        void shouldReturnUsersFromLocalAuthorityMappingWhenTheyDoNotExistInRefData() {
            when(organisationApi.findUsersInCurrentUserOrganisation(AUTH_TOKEN, SERVICE_AUTH_TOKEN, ACTIVE, false))
                .thenThrow(feignException(SC_NOT_FOUND));

            Set<String> usersIdsWithinSaLa = organisationService.findUserIdsInSameOrganisation("SA");

            assertThat(usersIdsWithinSaLa).containsExactlyInAnyOrder("1", "2", "3");
        }

        @Test
        void shouldReturnUsersFromLocalAuthorityMappingWhenRefDataFailsForReasonOtherThanUserNotRegistered() {
            when(organisationApi.findUsersInCurrentUserOrganisation(AUTH_TOKEN, SERVICE_AUTH_TOKEN, ACTIVE, false))
                .thenThrow(feignException(SC_INTERNAL_SERVER_ERROR));

            Set<String> usersIdsWithinSaLa = organisationService.findUserIdsInSameOrganisation("SA");

            assertThat(usersIdsWithinSaLa).containsExactlyInAnyOrder("1", "2", "3");
        }

        @Test
        void shouldReturnUsersFromOrganisationIfExistsInRefData() {
            OrganisationUsers usersInAnOrganisation = prepareUsersForAnOrganisation();
            when(organisationApi.findUsersInCurrentUserOrganisation(AUTH_TOKEN, SERVICE_AUTH_TOKEN, ACTIVE, false))
                .thenReturn(usersInAnOrganisation);

            Set<String> userIds = organisationService.findUserIdsInSameOrganisation("AN");

            assertThat(userIds).containsExactlyInAnyOrder("40", "41");
            verify(lookupSpy, never()).getUserIds(any());
        }

        @Test
        void shouldReturnEmptyListWhenTheLAIsNotKnownAndTheApiReturnsNotFound() {
            when(organisationApi.findUsersInCurrentUserOrganisation(any(), any(), any(), any()))
                .thenThrow(feignException(SC_FORBIDDEN));

            assertThatThrownBy(() -> organisationService.findUserIdsInSameOrganisation("AN"))
                .isInstanceOf(UserOrganisationLookupException.class)
                .hasMessage("Can't find users for AN local authority");
        }
    }

    @Nested
    class FindUserByEmail {

        @Test
        void shouldFindUser() {
            OrganisationUser user = new OrganisationUser(RandomStringUtils.randomAlphanumeric(10));

            when(organisationApi.findUserByEmail(AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_EMAIL)).thenReturn(user);

            Optional<String> actualUserId = organisationService.findUserByEmail(USER_EMAIL);

            assertThat(actualUserId).contains(user.getUserIdentifier());
        }

        @Test
        void shouldNotReturnUserIdIfUserNotPresent() {

            when(organisationApi.findUserByEmail(AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_EMAIL))
                .thenThrow(feignException(SC_NOT_FOUND));

            Optional<String> actualUserId = organisationService.findUserByEmail(USER_EMAIL);

            assertThat(actualUserId).isNotPresent();
        }

        @Test
        void shouldRethrowExceptionOtherThanNotFound() {
            String email = "test@test.com";

            when(organisationApi.findUserByEmail(AUTH_TOKEN, SERVICE_AUTH_TOKEN, email))
                .thenThrow(feignException(SC_INTERNAL_SERVER_ERROR, email));

            UserLookupException actualException = assertThrows(UserLookupException.class,
                () -> organisationService.findUserByEmail(email));

            assertThat(actualException)
                .hasMessageNotContaining(email)
                .hasMessageContaining("****@********");
        }
    }

    @Nested
    class FindUserOrganisation {

        @Test
        void shouldFindOrganisationWhenUserRegisteredInOrganisation() {
            when(organisationApi.findUserOrganisation(AUTH_TOKEN, SERVICE_AUTH_TOKEN))
                .thenReturn(POPULATED_ORGANISATION);

            Optional<Organisation> actualOrganisation = organisationService.findOrganisation();

            assertThat(actualOrganisation).contains(POPULATED_ORGANISATION);
        }

        @Test
        void shouldReturnEmptyOrganisationWhenUserNotRegisteredInOrganisation() {
            when(organisationApi.findUserOrganisation(AUTH_TOKEN, SERVICE_AUTH_TOKEN))
                .thenThrow(feignException(SC_FORBIDDEN));

            Optional<Organisation> organisation = organisationService.findOrganisation();

            assertThat(organisation).isEmpty();
        }

        @Test
        void shouldRethrowUnexpectedExceptions() {
            Exception expectedException = feignException(SC_GATEWAY_TIMEOUT);

            when(organisationApi.findUserOrganisation(AUTH_TOKEN, SERVICE_AUTH_TOKEN)).thenThrow(expectedException);

            Exception actualException = assertThrows(Exception.class, organisationService::findOrganisation);

            assertThat(actualException).isEqualTo(expectedException);
        }
    }

    @Nested
    class FindOrganisation {

        private static final String TOKEN = "token";
        private static final String ORGANISATION_ID = "ORGSA";

        @BeforeEach
        void init() {
            when(systemUserService.getSysUserToken()).thenReturn(TOKEN);
        }

        @Test
        void shouldFindOrganisationWhenExists() {
            when(organisationApi.findOrganisation(TOKEN, SERVICE_AUTH_TOKEN, ORGANISATION_ID))
                .thenReturn(POPULATED_ORGANISATION);

            Optional<Organisation> actualOrganisation = organisationService.findOrganisation(ORGANISATION_ID);

            assertThat(actualOrganisation).contains(POPULATED_ORGANISATION);
        }

        @Test
        void shouldReturnEmptyOrganisationWhenOrganisationDoesNotExists() {
            when(organisationApi.findOrganisation(TOKEN, SERVICE_AUTH_TOKEN, ORGANISATION_ID))
                .thenThrow(feignException(SC_FORBIDDEN));

            Optional<Organisation> organisation = organisationService.findOrganisation(ORGANISATION_ID);

            assertThat(organisation).isEmpty();
        }

        @Test
        void shouldRethrowUnexpectedExceptions() {
            Exception expectedException = feignException(SC_GATEWAY_TIMEOUT);

            when(organisationApi.findOrganisation(TOKEN, SERVICE_AUTH_TOKEN, ORGANISATION_ID))
                .thenThrow(expectedException);

            Exception actualException = assertThrows(Exception.class, () ->
                organisationService.findOrganisation(ORGANISATION_ID));

            assertThat(actualException).isEqualTo(expectedException);
        }
    }

    private static Organisation buildOrganisation() {
        return Organisation.builder()
            .name("Organisation")
            .organisationIdentifier(UUID.randomUUID().toString())
            .contactInformation(buildOrganisationContactInformation())
            .build();
    }

    private static List<ContactInformation> buildOrganisationContactInformation() {
        return List.of(ContactInformation.builder()
            .addressLine1("Flat 12, Pinnacle Apartments")
            .addressLine1("Saffron Central")
            .county("London")
            .country("United Kingdom")
            .postCode("CR0 2GE")
            .build());
    }

    private OrganisationUsers prepareUsersForAnOrganisation() {
        return new OrganisationUsers(List.of(
            OrganisationUser
                .builder()
                .userIdentifier("40")
                .build(),
            OrganisationUser
                .builder()
                .userIdentifier("41")
                .build()
        ));
    }
}
