package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RespondentPolicyData {
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

    public List<OrganisationPolicy> toList() {
        return List.of(
            respondentPolicy0,
            respondentPolicy1,
            respondentPolicy2,
            respondentPolicy3,
            respondentPolicy4,
            respondentPolicy5,
            respondentPolicy6,
            respondentPolicy7,
            respondentPolicy8,
            respondentPolicy9
        );
    }

    public List<OrganisationPolicy> diff(RespondentPolicyData respondentPolicyData) {
        List<OrganisationPolicy> original = this.toList();
        List<OrganisationPolicy> toCompare = respondentPolicyData.toList();

        return original.stream()
            .filter(element -> !toCompare.contains(element))
            .collect(Collectors.toList());
    }
}

