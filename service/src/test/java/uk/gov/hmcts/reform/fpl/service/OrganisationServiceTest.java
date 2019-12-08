package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrganisationServiceTest {

    @MockBean
    private LocalAuthorityUserLookupConfiguration laUserLookupConfig;
    @MockBean
    private OrganisationApi organisationApi;
    private OrganisationService organisationService;

    private static final String NA_USER_ID = "40";
    private static final String SW_USER_ID = "1";

    @BeforeEach
    void setup() {
        laUserLookupConfig = new LocalAuthorityUserLookupConfiguration("SA=>1,2,3");
        organisationService = new OrganisationService(laUserLookupConfig);
    }

    @Test
    void shouldReturnUsersFromLocalAuthorityMappingWhenTheyExist() {
        List<String> usersIdsWithinSaLa = organisationService.findUserIdsInSameOrganisation(SW_USER_ID, "SA");

        assertThat(usersIdsWithinSaLa)
            .containsExactlyInAnyOrder("1","2","3");
    }

    @Test
    void shouldReturnUserIdentifierWhenTheUserOrganisationCannotBeFound() {
        List<String> naUserIds = organisationService.findUserIdsInSameOrganisation(NA_USER_ID, "NA");

        assertThat(naUserIds)
            .containsExactly(NA_USER_ID);
    }

    @Test
    void shouldReturnUsersFromOrganisationIfExistsInRefData() {

    }

}
