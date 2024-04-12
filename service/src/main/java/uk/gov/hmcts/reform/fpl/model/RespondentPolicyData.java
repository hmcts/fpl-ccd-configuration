package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RespondentPolicyData implements PolicyData {
    OrganisationPolicy respondentPolicy0;
    OrganisationPolicy respondentPolicy1;
    OrganisationPolicy respondentPolicy2;
    OrganisationPolicy respondentPolicy3;
    OrganisationPolicy respondentPolicy4;
    OrganisationPolicy respondentPolicy5;
    OrganisationPolicy respondentPolicy6;
    OrganisationPolicy respondentPolicy7;
    OrganisationPolicy respondentPolicy8;
    OrganisationPolicy respondentPolicy9;

    @Override
    @JsonIgnore
    public OrganisationPolicy[] getAllPolicies() {
        return new OrganisationPolicy[] {
            respondentPolicy0, respondentPolicy1, respondentPolicy2, respondentPolicy3, respondentPolicy4,
            respondentPolicy5, respondentPolicy6, respondentPolicy7, respondentPolicy8, respondentPolicy9
        };
    }
}

