package uk.gov.hmcts.reform.ccd.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.CREATOR;

class OrganisationPolicyTest {

    @Test
    void shouldCreateOrganisationPolicy() {
        final OrganisationPolicy actualOrgPolicy
            = OrganisationPolicy.organisationPolicy("ORG1", "Private solicitor", CREATOR);

        final OrganisationPolicy expectedOrgPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole("[CREATOR]")
            .organisation(Organisation.builder()
                .organisationID("ORG1")
                .organisationName("Private solicitor")
                .build())
            .build();

        assertThat(actualOrgPolicy).isEqualTo(expectedOrgPolicy);
    }

    @Test
    void shouldReturnNullIfOrganisationIdIsNull() {
        final OrganisationPolicy actualOrgPolicy
            = OrganisationPolicy.organisationPolicy(null, null, CREATOR);

        assertThat(actualOrgPolicy).isNull();
    }
}
