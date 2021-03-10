package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.ccd.model.Organisation;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganisationPolicyStash {

    private String organisationId;
    private String organisationName;
    private String reference;
    private String role;

    public uk.gov.hmcts.reform.ccd.model.OrganisationPolicy toOrganisationPolicy() {

        return uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                .organisationID(organisationId)
                .organisationName(organisationName)
                .build())
            .orgPolicyCaseAssignedRole(role)
            .orgPolicyReference(reference)
            .build();
    }

    public static OrganisationPolicyStash from(uk.gov.hmcts.reform.ccd.model.OrganisationPolicy policy) {

        return OrganisationPolicyStash.builder()
            .organisationId(policy.getOrganisation().getOrganisationID())
            .organisationName(policy.getOrganisation().getOrganisationName())
            .reference(policy.getOrgPolicyReference())
            .role(policy.getOrgPolicyCaseAssignedRole())
            .build();
    }
}

