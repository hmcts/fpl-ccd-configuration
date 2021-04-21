package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;

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
}

