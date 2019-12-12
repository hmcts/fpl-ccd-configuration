package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.fpl.exceptions.UserOrganisationLookupException;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Status;
import uk.gov.hmcts.reform.rd.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class OrganisationServiceTest {

    @MockBean
    private OrganisationApi organisationApi;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    private OrganisationService organisationService;

    private static final String AUTH_TOKEN_ID = "Bearer authorisedBearer";
    private static final String SERVICE_AUTH_TOKEN_ID = "Bearer authorised service";

    @BeforeEach
    void setup() {
        LocalAuthorityUserLookupConfiguration laUserLookupConfig =
            new LocalAuthorityUserLookupConfiguration("SA=>1,2,3");
        organisationService = new OrganisationService(laUserLookupConfig, organisationApi, authTokenGenerator);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN_ID);
    }

    @Test
    void shouldReturnUsersFromLocalAuthorityMappingWhenTheyExist() {
        List<String> usersIdsWithinSaLa = organisationService
            .findUserIdsInSameOrganisation(AUTH_TOKEN_ID, "SA");

        assertThat(usersIdsWithinSaLa)
            .containsExactlyInAnyOrder("1", "2", "3");
    }

    @Test
    void shouldReturnUsersFromOrganisationIfExistsInRefData() {
        List<User> usersInAnOrganisation = prepareUsersForAnOrganisation();
        when(organisationApi.findUsersByOrganisation(AUTH_TOKEN_ID, SERVICE_AUTH_TOKEN_ID, Status.ACTIVE))
            .thenReturn(usersInAnOrganisation);

        List<String> userIds = organisationService
            .findUserIdsInSameOrganisation(AUTH_TOKEN_ID, "AN");

        assertThat(userIds)
            .containsExactly("40", "41");
    }

    @Test
    void shouldReturnEmptyListWhenTheLAIsNotKnownAndTheApiReturnsNotFound() {
        when(organisationApi.findUsersByOrganisation(any(), any(), any()))
            .thenThrow(new FeignException.NotFound("No organisation", new byte[]{}));

        AssertionsForClassTypes.assertThatThrownBy(() ->
            organisationService
                .findUserIdsInSameOrganisation(AUTH_TOKEN_ID, "AN"))
            .isInstanceOf(UserOrganisationLookupException.class)
            .hasMessage("Can't find users for AN local authority");
    }

    private List<User> prepareUsersForAnOrganisation() {
        return List.of(
            User
                .builder()
                .userIdentifier("40")
                .build(),
            User
                .builder()
                .userIdentifier("41")
                .build()
        );
    }
}
