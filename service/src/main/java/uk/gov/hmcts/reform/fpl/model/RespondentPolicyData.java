package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerApproverCrudPlus2RolesNhaismAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORARAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORBRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORCRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORDRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORERAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORFRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORGRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORHRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORIRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.SOLICITORJRAccess;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RespondentPolicyData implements PolicyData {
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, SOLICITORARAccess.class})
    OrganisationPolicy respondentPolicy0;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, SOLICITORBRAccess.class})
    OrganisationPolicy respondentPolicy1;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, SOLICITORCRAccess.class})
    OrganisationPolicy respondentPolicy2;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, SOLICITORDRAccess.class})
    OrganisationPolicy respondentPolicy3;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, SOLICITORERAccess.class})
    OrganisationPolicy respondentPolicy4;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, SOLICITORFRAccess.class})
    OrganisationPolicy respondentPolicy5;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, SOLICITORGRAccess.class})
    OrganisationPolicy respondentPolicy6;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, SOLICITORHRAccess.class})
    OrganisationPolicy respondentPolicy7;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, SOLICITORIRAccess.class})
    OrganisationPolicy respondentPolicy8;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, SOLICITORJRAccess.class})
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

