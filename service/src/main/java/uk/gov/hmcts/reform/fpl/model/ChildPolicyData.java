package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ChildPolicyData {
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
}

