package uk.gov.hmcts.reform.ccd.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrganisationTest {

    @Test
    void shouldCreateOrganisation() {
        final Organisation actualOrganisation = Organisation.organisation("orgId");

        final Organisation expectedOrganisation = Organisation.builder()
            .organisationID("orgId")
            .build();

        assertThat(actualOrganisation).isEqualTo(expectedOrganisation);
    }
}
