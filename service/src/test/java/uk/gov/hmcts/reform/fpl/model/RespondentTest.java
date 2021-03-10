package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class RespondentTest {

    @Test
    void shouldStashOrganisationPolicy() {
        Respondent respondent = Respondent.builder()
            .organisationPolicy(OrganisationPolicy.builder()
                .orgPolicyReference("REF")
                .orgPolicyCaseAssignedRole("[SOLICITOR]")
                .organisation(Organisation.builder()
                    .organisationID("ORGID")
                    .organisationName("Org name")
                    .build())
                .build())
            .build();

        respondent.stashOrganisationPolicy();

        assertThat(respondent.getOrganisationPolicyStash()).isEqualTo(OrganisationPolicyStash.builder()
            .organisationId("ORGID")
            .organisationName("Org name")
            .reference("REF")
            .role("[SOLICITOR]")
            .build());

        assertThat(respondent.getOrganisationPolicy()).isNull();
    }

    @Test
    void shouldRestoreOrganisationPolicy() {
        Respondent respondent = Respondent.builder()
            .organisationPolicyStash(OrganisationPolicyStash.builder()
                .organisationId("ORGID")
                .organisationName("Org name")
                .reference("REF")
                .role("[SOLICITOR]")
                .build())
            .build();

        respondent.restoreOrganisationPolicyStash();

        assertThat(respondent.getOrganisationPolicy()).isEqualTo(OrganisationPolicy.builder()
            .orgPolicyReference("REF")
            .orgPolicyCaseAssignedRole("[SOLICITOR]")
            .organisation(Organisation.builder()
                .organisationID("ORGID")
                .organisationName("Org name")
                .build())
            .build());
    }
}
