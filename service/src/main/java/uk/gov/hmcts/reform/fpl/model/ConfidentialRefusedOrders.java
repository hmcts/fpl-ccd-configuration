package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess;

@Data
@Builder
public class ConfidentialRefusedOrders implements ConfidentialOrderBundle<HearingOrder> {

    @CCD(
            label = "Refused Order (Uploaded by CTSC)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersCTSC;
    @CCD(
            label = "Refused Order (Uploaded by LA)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersLA;
    @CCD(
            label = "Refused Order (Uploaded by Respondent 1)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersResp0;
    @CCD(
            label = "Refused Order (Uploaded by Respondent 2)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersResp1;
    @CCD(
            label = "Refused Order (Uploaded by Respondent 3)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersResp2;
    @CCD(
            label = "Refused Order (Uploaded by Respondent 4)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersResp3;
    @CCD(
            label = "Refused Order (Uploaded by Respondent 5)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersResp4;
    @CCD(
            label = "Refused Order (Uploaded by Respondent 6)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersResp5;
    @CCD(
            label = "Refused Order (Uploaded by Respondent 7)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersResp6;
    @CCD(
            label = "Refused Order (Uploaded by Respondent 8)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersResp7;
    @CCD(
            label = "Refused Order (Uploaded by Respondent 9)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersResp8;
    @CCD(
            label = "Refused Order (Uploaded by Respondent 10)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersResp9;
    @CCD(
            label = "Refused Order (Uploaded by Child 1)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild0;
    @CCD(
            label = "Refused Order (Uploaded by Child 2)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild1;
    @CCD(
            label = "Refused Order (Uploaded by Child 3)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild2;
    @CCD(
            label = "Refused Order (Uploaded by Child 4)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild3;
    @CCD(
            label = "Refused Order (Uploaded by Child 5)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild4;
    @CCD(
            label = "Refused Order (Uploaded by Child 6)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild5;
    @CCD(
            label = "Refused Order (Uploaded by Child 7)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild6;
    @CCD(
            label = "Refused Order (Uploaded by Child 8)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild7;
    @CCD(
            label = "Refused Order (Uploaded by Child 9)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild8;
    @CCD(
            label = "Refused Order (Uploaded by Child 10)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild9;
    @CCD(
            label = "Refused Order (Uploaded by Child 11)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild10;
    @CCD(
            label = "Refused Order (Uploaded by Child 12)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild11;
    @CCD(
            label = "Refused Order (Uploaded by Child 13)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild12;
    @CCD(
            label = "Refused Order (Uploaded by Child 14)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild13;
    @CCD(
            label = "Refused Order (Uploaded by Child 15)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadedCaseManagementOrder",
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class}
    )
    private List<Element<HearingOrder>> refusedHearingOrdersChild14;

    @Override
    public String getFieldBaseName() {
        return "refusedHearingOrders";
    }
}
