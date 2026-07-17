package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class ConfidentialGeneratedOrders implements ConfidentialOrderBundle<GeneratedOrder> {
    @CCD(
            label = "Confidential Order (Uploaded by CTSC)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionCTSC;
    @CCD(
            label = "Confidential Order (Uploaded by LA)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionLA;
    @CCD(
            label = "Confidential Order (Uploaded by Respondent 1)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionResp0;
    @CCD(
            label = "Confidential Order (Uploaded by Respondent 2)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionResp1;
    @CCD(
            label = "Confidential Order (Uploaded by Respondent 3)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionResp2;
    @CCD(
            label = "Confidential Order (Uploaded by Respondent 4)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionResp3;
    @CCD(
            label = "Confidential Order (Uploaded by Respondent 5)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionResp4;
    @CCD(
            label = "Confidential Order (Uploaded by Respondent 6)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionResp5;
    @CCD(
            label = "Confidential Order (Uploaded by Respondent 7)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionResp6;
    @CCD(
            label = "Confidential Order (Uploaded by Respondent 8)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionResp7;
    @CCD(
            label = "Confidential Order (Uploaded by Respondent 9)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionResp8;
    @CCD(
            label = "Confidential Order (Uploaded by Respondent 10)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawSuperuserCuAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionResp9;
    @CCD(
            label = "Confidential Order (Uploaded by Child 1)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild0;
    @CCD(
            label = "Confidential Order (Uploaded by Child 2)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild1;
    @CCD(
            label = "Confidential Order (Uploaded by Child 3)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild2;
    @CCD(
            label = "Confidential Order (Uploaded by Child 4)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild3;
    @CCD(
            label = "Confidential Order (Uploaded by Child 5)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild4;
    @CCD(
            label = "Confidential Order (Uploaded by Child 6)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild5;
    @CCD(
            label = "Confidential Order (Uploaded by Child 7)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild6;
    @CCD(
            label = "Confidential Order (Uploaded by Child 8)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild7;
    @CCD(
            label = "Confidential Order (Uploaded by Child 9)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild8;
    @CCD(
            label = "Confidential Order (Uploaded by Child 10)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild9;
    @CCD(
            label = "Confidential Order (Uploaded by Child 11)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild10;
    @CCD(
            label = "Confidential Order (Uploaded by Child 12)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild11;
    @CCD(
            label = "Confidential Order (Uploaded by Child 13)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild12;
    @CCD(
            label = "Confidential Order (Uploaded by Child 14)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild13;
    @CCD(
            label = "Confidential Order (Uploaded by Child 15)",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCuPlus4RolesRenpooAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesIjkqokAccess.class}
    )
    private List<Element<GeneratedOrder>> orderCollectionChild14;

    public String getFieldBaseName() {
        return "orderCollection";
    }
}
