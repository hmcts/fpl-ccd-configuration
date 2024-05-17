package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ChildPolicyData implements PolicyData {
    OrganisationPolicy childPolicy0;
    OrganisationPolicy childPolicy1;
    OrganisationPolicy childPolicy2;
    OrganisationPolicy childPolicy3;
    OrganisationPolicy childPolicy4;
    OrganisationPolicy childPolicy5;
    OrganisationPolicy childPolicy6;
    OrganisationPolicy childPolicy7;
    OrganisationPolicy childPolicy8;
    OrganisationPolicy childPolicy9;
    OrganisationPolicy childPolicy10;
    OrganisationPolicy childPolicy11;
    OrganisationPolicy childPolicy12;
    OrganisationPolicy childPolicy13;
    OrganisationPolicy childPolicy14;

    @Override
    @JsonIgnore
    public OrganisationPolicy[] getAllPolicies() {
        return new OrganisationPolicy[] {
            childPolicy0, childPolicy1, childPolicy2, childPolicy3, childPolicy4, childPolicy5, childPolicy6,
            childPolicy7, childPolicy8, childPolicy9, childPolicy10, childPolicy11, childPolicy12, childPolicy13,
            childPolicy14
        };
    }
}

