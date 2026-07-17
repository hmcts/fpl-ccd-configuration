package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerApproverCrudPlus2RolesNhaismAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORARAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORBRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORCRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORDRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORERAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORFRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORGRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORHRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORIRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORJRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORKRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORLRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORMRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORNRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORORAccess;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ChildPolicyData implements PolicyData {
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORARAccess.class})
    OrganisationPolicy childPolicy0;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORBRAccess.class})
    OrganisationPolicy childPolicy1;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORCRAccess.class})
    OrganisationPolicy childPolicy2;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORDRAccess.class})
    OrganisationPolicy childPolicy3;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORERAccess.class})
    OrganisationPolicy childPolicy4;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORFRAccess.class})
    OrganisationPolicy childPolicy5;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORGRAccess.class})
    OrganisationPolicy childPolicy6;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORHRAccess.class})
    OrganisationPolicy childPolicy7;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORIRAccess.class})
    OrganisationPolicy childPolicy8;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORJRAccess.class})
    OrganisationPolicy childPolicy9;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORKRAccess.class})
    OrganisationPolicy childPolicy10;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORLRAccess.class})
    OrganisationPolicy childPolicy11;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORMRAccess.class})
    OrganisationPolicy childPolicy12;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORNRAccess.class})
    OrganisationPolicy childPolicy13;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus2RolesNhaismAccess.class, CHILDSOLICITORORAccess.class})
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

