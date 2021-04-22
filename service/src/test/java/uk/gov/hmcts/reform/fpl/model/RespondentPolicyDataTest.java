package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RespondentPolicyDataTest {

    @Test
    void shouldReturnListOfDifferingRespondentPoliciesWhenNotMatching() {
        OrganisationPolicy updatedOrganisationPolicy0 = buildOrganisationPolicy("BA111");
        OrganisationPolicy updatedOrganisationPolicy6 = buildOrganisationPolicy("CC111");

        RespondentPolicyData respondentPolicyData = RespondentPolicyData.builder()
            .respondentPolicy0(updatedOrganisationPolicy0)
            .respondentPolicy1(buildOrganisationPolicy("WA112"))
            .respondentPolicy2(buildOrganisationPolicy("WA113"))
            .respondentPolicy3(buildOrganisationPolicy("WA114"))
            .respondentPolicy4(buildOrganisationPolicy("WA115"))
            .respondentPolicy5(buildOrganisationPolicy("WA116"))
            .respondentPolicy6(updatedOrganisationPolicy6)
            .respondentPolicy7(buildOrganisationPolicy("WA118"))
            .respondentPolicy8(buildOrganisationPolicy("WA119"))
            .respondentPolicy9(buildOrganisationPolicy("WA110"))
            .build();

        RespondentPolicyData respondentPolicyDataBefore = buildRespondentRespondentData();

        List<OrganisationPolicy> differingOrgPolicies = respondentPolicyData.diff(respondentPolicyDataBefore);

        assertThat(differingOrgPolicies).isEqualTo(List.of(
            updatedOrganisationPolicy0,
            updatedOrganisationPolicy6
        ));
    }

    @Test
    void shouldReturnEmptyListWhenRespondentPoliciesMatch() {
        RespondentPolicyData respondentPolicyData = buildRespondentRespondentData();
        RespondentPolicyData respondentPolicyDataBefore = buildRespondentRespondentData();

        List<OrganisationPolicy> differingOrgPolicies = respondentPolicyData.diff(respondentPolicyDataBefore);

        assertThat(differingOrgPolicies).isEmpty();
    }

    private RespondentPolicyData buildRespondentRespondentData() {
        return RespondentPolicyData.builder()
            .respondentPolicy0(buildOrganisationPolicy("WA111"))
            .respondentPolicy1(buildOrganisationPolicy("WA112"))
            .respondentPolicy2(buildOrganisationPolicy("WA113"))
            .respondentPolicy3(buildOrganisationPolicy("WA114"))
            .respondentPolicy4(buildOrganisationPolicy("WA115"))
            .respondentPolicy5(buildOrganisationPolicy("WA116"))
            .respondentPolicy6(buildOrganisationPolicy("WA117"))
            .respondentPolicy7(buildOrganisationPolicy("WA118"))
            .respondentPolicy8(buildOrganisationPolicy("WA119"))
            .respondentPolicy9(buildOrganisationPolicy("WA110"))
            .build();
    }

    private OrganisationPolicy buildOrganisationPolicy(String organisationId) {
        return OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                .organisationID(organisationId)
                .build())
            .build();
    }
}
