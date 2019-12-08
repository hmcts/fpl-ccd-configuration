package uk.gov.hmcts.reform.fpl.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
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

    private static final String NA_USER_ID = "40";
    private static final String SW_USER_ID = "1";

    private static final String AUTH_TOKEN_ID = "Bearer authorisedBearer";
    private static final String SERVICE_AUTH_TOKEN_ID = "Bearer authorised service";

    @BeforeEach
    void setup() {
        LocalAuthorityUserLookupConfiguration laUserLookupConfig = new LocalAuthorityUserLookupConfiguration("SA=>1,2,3");
        organisationService = new OrganisationService(laUserLookupConfig, organisationApi, authTokenGenerator);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN_ID);
    }

    @Test
    void shouldReturnUsersFromLocalAuthorityMappingWhenTheyExist() {
        when(organisationApi.findUsersByOrganisation(any(), any(), any())).thenThrow(new FeignException.NotFound("No organisation", new byte[]{}));
        List<String> usersIdsWithinSaLa = organisationService.findUserIdsInSameOrganisation(AUTH_TOKEN_ID, SW_USER_ID, "SA");

        assertThat(usersIdsWithinSaLa)
            .containsExactlyInAnyOrder("1","2","3");
    }

    @Test
    void shouldReturnUserIdentifierWhenTheUserOrganisationCannotBeFound() {
        when(organisationApi.findUsersByOrganisation(any(), any(), any())).thenThrow(new FeignException.NotFound("No organisation", new byte[]{}));
        List<String> naUserIds = organisationService.findUserIdsInSameOrganisation(AUTH_TOKEN_ID, NA_USER_ID, "NA");

        assertThat(naUserIds)
            .containsExactly(NA_USER_ID);
    }

    @Test
    void shouldReturnUsersFromOrganisationIfExistsInRefData() {
        List<User> usersInNaOrganisation = List.of(new User(NA_USER_ID), new User("41"));
        when(organisationApi.findUsersByOrganisation(AUTH_TOKEN_ID, SERVICE_AUTH_TOKEN_ID, Status.ACTIVE))
           .thenReturn(usersInNaOrganisation);

        List<String> naUserIds = organisationService.findUserIdsInSameOrganisation(AUTH_TOKEN_ID, NA_USER_ID, "NA");

        assertThat(naUserIds)
            .containsExactly(NA_USER_ID, "41");
    }

}
