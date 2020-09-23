package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import feign.Request;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
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
import uk.gov.hmcts.reform.rd.model.Status;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class OrganisationServiceTest {

    @Mock
    private OrganisationApi organisationApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private RequestData requestData;

    @Spy
    private final LocalAuthorityUserLookupConfiguration lookupSpy = new LocalAuthorityUserLookupConfiguration(
        "SA=>1,2,3"
    );

    @InjectMocks
    private OrganisationService organisationService;

    private static final Request REQUEST = Request.create(GET, EMPTY, Map.of(), new byte[]{}, UTF_8, null);
    private static final String AUTH_TOKEN_ID = "Bearer authorisedBearer";
    private static final String SERVICE_AUTH_TOKEN_ID = "Bearer authorised service";
    private static final String USER_EMAIL = "test@test.com";
    private static final Organisation EMPTY_ORGANISATION = Organisation.builder().build();
    private static final Organisation POPULATED_ORGANISATION = buildOrganisation();

    @BeforeEach
    void setup() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN_ID);
        when(requestData.authorisation()).thenReturn(AUTH_TOKEN_ID);
    }

    @Test
    void shouldReturnUsersFromLocalAuthorityMappingWhenTheyDoNotExistInRefData() {
        when(organisationApi.findUsersByOrganisation(AUTH_TOKEN_ID, SERVICE_AUTH_TOKEN_ID, Status.ACTIVE, false))
            .thenThrow(new FeignException.NotFound(EMPTY, REQUEST, new byte[]{}));

        Set<String> usersIdsWithinSaLa = organisationService.findUserIdsInSameOrganisation("SA");

        assertThat(usersIdsWithinSaLa).containsExactlyInAnyOrder("1", "2", "3");
    }

    @Test
    void shouldReturnUsersFromLocalAuthorityMappingWhenRefDataFailsForReasonOtherThanUserNotRegistered() {
        when(organisationApi.findUsersByOrganisation(AUTH_TOKEN_ID, SERVICE_AUTH_TOKEN_ID, Status.ACTIVE, false))
            .thenThrow(new FeignException.InternalServerError(EMPTY, REQUEST, new byte[]{}));

        Set<String> usersIdsWithinSaLa = organisationService.findUserIdsInSameOrganisation("SA");

        assertThat(usersIdsWithinSaLa).containsExactlyInAnyOrder("1", "2", "3");
    }

    @Test
    void shouldReturnUsersFromOrganisationIfExistsInRefData() {
        OrganisationUsers usersInAnOrganisation = prepareUsersForAnOrganisation();
        when(organisationApi.findUsersByOrganisation(AUTH_TOKEN_ID, SERVICE_AUTH_TOKEN_ID, Status.ACTIVE, false))
            .thenReturn(usersInAnOrganisation);

        Set<String> userIds = organisationService.findUserIdsInSameOrganisation("AN");

        assertThat(userIds).containsExactlyInAnyOrder("40", "41");
        verify(lookupSpy, never()).getUserIds(any());
    }

    @Test
    void shouldReturnEmptyListWhenTheLAIsNotKnownAndTheApiReturnsNotFound() {
        when(organisationApi.findUsersByOrganisation(any(), any(), any(), any()))
            .thenThrow(new FeignException.Forbidden("No organisation", REQUEST, new byte[]{}));

        assertThatThrownBy(() -> organisationService.findUserIdsInSameOrganisation("AN"))
            .isInstanceOf(UserOrganisationLookupException.class)
            .hasMessage("Can't find users for AN local authority");
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

    @Test
    void shouldFindUser() {
        OrganisationUser user = new OrganisationUser(RandomStringUtils.randomAlphanumeric(10));

        when(organisationApi.findUserByEmail(AUTH_TOKEN_ID, SERVICE_AUTH_TOKEN_ID, USER_EMAIL)).thenReturn(user);

        Optional<String> actualUserId = organisationService.findUserByEmail(USER_EMAIL);

        assertThat(actualUserId).isEqualTo(Optional.of(user.getUserIdentifier()));
    }

    @Test
    void shouldNotReturnUserIdIfUserNotPresent() {
        Exception exception = new FeignException.NotFound(EMPTY, REQUEST, new byte[]{});

        when(organisationApi.findUserByEmail(AUTH_TOKEN_ID, SERVICE_AUTH_TOKEN_ID, USER_EMAIL)).thenThrow(exception);

        Optional<String> actualUserId = organisationService.findUserByEmail(USER_EMAIL);

        assertThat(actualUserId.isPresent()).isFalse();
    }

    @Test
    void shouldRethrowExceptionOtherThanNotFound() {
        String email = "test@test.com";
        Exception exception = new FeignException.InternalServerError(email, REQUEST, new byte[]{});

        when(organisationApi.findUserByEmail(AUTH_TOKEN_ID, SERVICE_AUTH_TOKEN_ID, email)).thenThrow(exception);

        UserLookupException actualException = assertThrows(UserLookupException.class,
            () -> organisationService.findUserByEmail(email));

        assertThat(actualException)
            .hasMessageNotContaining(email)
            .hasMessageContaining("****@********");
    }

    @Test
    void shouldFindOrganisation() {
        when(organisationApi.findOrganisationById(AUTH_TOKEN_ID, SERVICE_AUTH_TOKEN_ID))
            .thenReturn(POPULATED_ORGANISATION);

        Organisation actualOrganisation = organisationService.findOrganisation();

        assertThat(actualOrganisation).isEqualTo(POPULATED_ORGANISATION);
    }

    @Test
    void shouldReturnEmptyOrganisationBuilderWhenOrganisationNotFound() {
        when(organisationApi.findOrganisationById(AUTH_TOKEN_ID, SERVICE_AUTH_TOKEN_ID))
            .thenThrow(new FeignException.NotFound("Organisation not found", REQUEST, new byte[]{}));

        Organisation organisation = organisationService.findOrganisation();

        assertThat(organisation).isEqualTo(EMPTY_ORGANISATION);
    }

    @Test
    void shouldThrowFeignExceptionWhenOrganisationIsNotFound() {
        when(organisationApi.findOrganisationById(AUTH_TOKEN_ID, SERVICE_AUTH_TOKEN_ID))
            .thenThrow(new FeignException.NotFound("Organisation not found", REQUEST, new byte[]{}));

        assertThatThrownBy(() -> organisationApi.findOrganisationById(AUTH_TOKEN_ID, SERVICE_AUTH_TOKEN_ID))
            .isInstanceOf(FeignException.NotFound.class);
    }

    private static Organisation buildOrganisation() {
        return Organisation.builder()
            .name("Organisation")
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
}
