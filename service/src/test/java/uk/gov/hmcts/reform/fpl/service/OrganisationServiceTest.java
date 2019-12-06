package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrganisationServiceTest {

    @MockBean
    private LocalAuthorityUserLookupConfiguration laUserLookupConfig;
    private OrganisationService organisationService;

    private static final String AUTH_TOKEN = "1";

    @BeforeEach
    void setup() {
        laUserLookupConfig = new LocalAuthorityUserLookupConfiguration("SA=>1,2,3");
        organisationService = new OrganisationService(laUserLookupConfig);
    }

    @Test
    public void shouldReturnUsersFromLocalAuthorityMappingWhenTheyExist() {
        List<String> usersIdsWithinSaLa = organisationService.findUserIdsInSameOrganisation(AUTH_TOKEN, "SA");

        assertThat(usersIdsWithinSaLa)
            .containsExactlyInAnyOrder("1","2","3");
    }

}
