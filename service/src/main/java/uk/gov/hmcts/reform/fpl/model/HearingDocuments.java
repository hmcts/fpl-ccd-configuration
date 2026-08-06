package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCruPlus40RolesDyoppfAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperRuPlus2RolesOrmxlgAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCrudPlus2RolesXzgsvpAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcassRuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminRuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORBUPlus23RolesLfbtswAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERUPlus9RolesUvllojAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperUPlus2RolesWyvkcaAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminUAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserUAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateUAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCruPlus40RolesFdpfqlAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperRPlus3RolesWbcsaxAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCaseworkerPubliclawCourtadminCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcasssystemupdateRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACruPlus28RolesNrpimkAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcassRPlus5RolesLfrbycAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawJudiciaryRuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateRuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateRuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGCruPlus4RolesFtwwcyAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERRPlus38RolesGedyfhAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcassCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGRPlus8RolesQbtompAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCruPlus40RolesRcmaiaAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCruPlus2RolesBskdreAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateCruCaseworkerPubliclawSuperuserCrudAccess;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingDocuments {
    @CCD(label = "Court bundle", searchable = false, access = {BARRISTERCruPlus40RolesDyoppfAccess.class})
    private final List<Element<HearingCourtBundle>> courtBundleListV2;
    @CCD(
            label = "Court bundle",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingCourtBundleConfidential",
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawGatekeeperRuPlus2RolesOrmxlgAccess.class, LABARRISTERCrudPlus2RolesXzgsvpAccess.class, CaseworkerPubliclawCafcassRuAccess.class, CaseworkerPubliclawCourtadminRuAccess.class}
    )
    private final List<Element<HearingCourtBundle>> courtBundleListLA;
    @CCD(
            label = "Court bundle",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingCourtBundleConfidential",
            access = {CaseworkerPubliclawGatekeeperRuPlus2RolesOrmxlgAccess.class, CaseworkerPubliclawCourtadminRuAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    private final List<Element<HearingCourtBundle>> courtBundleListCTSC;
    @CCD(
            label = "Court bundle",
            searchable = false,
            access = {CHILDSOLICITORBUPlus23RolesLfbtswAccess.class, BARRISTERUPlus9RolesUvllojAccess.class, CaseworkerPubliclawGatekeeperUPlus2RolesWyvkcaAccess.class, CaseworkerPubliclawCourtadminUAccess.class, CaseworkerPubliclawSuperuserUAccess.class, CaseworkerPubliclawSystemupdateUAccess.class}
    )
    private final List<Element<HearingCourtBundle>> courtBundleListRemoved;

    @CCD(label = "Case summary", searchable = false, access = {BARRISTERCruPlus40RolesFdpfqlAccess.class})
    private final List<Element<CaseSummary>> caseSummaryList;
    @CCD(
            label = "Case summary",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CaseSummaryConfidential",
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawGatekeeperRPlus3RolesWbcsaxAccess.class, LABARRISTERCaseworkerPubliclawCourtadminCruAccess.class, CaseworkerPubliclawCafcassRuAccess.class, CaseworkerPubliclawCafcasssystemupdateRAccess.class}
    )
    private final List<Element<CaseSummary>> caseSummaryListLA;
    @CCD(
            label = "Case summary",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CaseSummaryConfidential",
            access = {CaseworkerPubliclawGatekeeperRPlus3RolesWbcsaxAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final List<Element<CaseSummary>> caseSummaryListCTSC;
    @CCD(label = "Case summary", searchable = false)
    private final List<Element<CaseSummary>> caseSummaryListRemoved;

    @CCD(
            label = "Position statement child",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CaseworkerPubliclawCafcassRPlus5RolesLfrbycAccess.class}
    )
    private final List<Element<PositionStatementChild>> posStmtChildList;
    @CCD(
            label = "Position statement child",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PositionStatementChildConfidential",
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCafcassRPlus5RolesLfrbycAccess.class, LABARRISTERCaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final List<Element<PositionStatementChild>> posStmtChildListLA;
    @CCD(
            label = "Position statement child",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PositionStatementChildConfidential",
            access = {CaseworkerPubliclawCourtadminCruAccess.class, CaseworkerPubliclawGatekeeperRAccess.class, CaseworkerPubliclawJudiciaryRuAccess.class, CaseworkerPubliclawMagistrateRuAccess.class, CaseworkerPubliclawSystemupdateRuAccess.class}
    )
    private final List<Element<PositionStatementChild>> posStmtChildListCTSC;
    @CCD(
            label = "Position statement child",
            searchable = false,
            access = {CaseworkerPubliclawSystemupdateRuAccess.class}
    )
    private final List<Element<PositionStatementChild>> posStmtChildListRemoved;

    @CCD(
            label = "Position statement respondent",
            searchable = false,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CaseworkerPubliclawCafcassRPlus5RolesLfrbycAccess.class}
    )
    private final List<Element<PositionStatementRespondent>> posStmtRespList;
    @CCD(
            label = "Position statement respondent",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PositionStatementRespondentConfidential",
            access = {CaseworkerPubliclawCafcassRPlus5RolesLfrbycAccess.class, EPSMANAGINGCruPlus4RolesFtwwcyAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    private final List<Element<PositionStatementRespondent>> posStmtRespListLA;
    @CCD(
            label = "Position statement respondent",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PositionStatementRespondentConfidential",
            access = {CaseworkerPubliclawCourtadminCruAccess.class, CaseworkerPubliclawGatekeeperRAccess.class, CaseworkerPubliclawJudiciaryRuAccess.class, CaseworkerPubliclawMagistrateRuAccess.class, CaseworkerPubliclawSystemupdateRuAccess.class}
    )
    private final List<Element<PositionStatementRespondent>> posStmtRespListCTSC;
    @CCD(
            label = "Position statement respondent",
            searchable = false,
            access = {CaseworkerPubliclawSystemupdateRuAccess.class}
    )
    private final List<Element<PositionStatementRespondent>> posStmtRespListRemoved;

    @CCD(
            label = "Position statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PositionStatement",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    private final List<Element<ManagedDocument>> posStmtList;
    @CCD(
            label = "Position statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PositionStatementConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    private final List<Element<ManagedDocument>> posStmtListLA;
    @CCD(
            label = "Position statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PositionStatementConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private final List<Element<ManagedDocument>> posStmtListCTSC;
    @CCD(
            label = "Position statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PositionStatement",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    private final List<Element<PositionStatement>> posStmtListRemoved;

    @CCD(label = "Skeleton argument", searchable = false, access = {BARRISTERCruPlus40RolesRcmaiaAccess.class})
    private final List<Element<SkeletonArgument>> skeletonArgumentList;
    @CCD(
            label = "Skeleton argument",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "SkeletonArgumentConfidential",
            access = {DefaultAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, LABARRISTERCruPlus2RolesBskdreAccess.class, CaseworkerPubliclawMagistrateCruCaseworkerPubliclawSuperuserCrudAccess.class}
    )
    private final List<Element<SkeletonArgument>> skeletonArgumentListLA;
    @CCD(
            label = "Skeleton argument",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "SkeletonArgumentConfidential",
            access = {DefaultAccess.class, CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawMagistrateCruCaseworkerPubliclawSuperuserCrudAccess.class}
    )
    private final List<Element<SkeletonArgument>> skeletonArgumentListCTSC;
    @CCD(label = "Skeleton argument", searchable = false)
    private final List<Element<SkeletonArgument>> skeletonArgumentListRemoved;

    public List<Element<HearingCourtBundle>> getCourtBundleListV2() {
        return defaultIfNull(courtBundleListV2, new ArrayList<>());
    }

    public List<Element<HearingCourtBundle>> getCourtBundleListLA() {
        return defaultIfNull(courtBundleListLA, new ArrayList<>());
    }

    public List<Element<HearingCourtBundle>> getCourtBundleListCTSC() {
        return defaultIfNull(courtBundleListCTSC, new ArrayList<>());
    }

    public List<Element<HearingCourtBundle>> getCourtBundleListRemoved() {
        return defaultIfNull(courtBundleListRemoved, new ArrayList<>());
    }

    public List<Element<CaseSummary>> getCaseSummaryList() {
        return defaultIfNull(caseSummaryList, new ArrayList<>());
    }

    public List<Element<CaseSummary>> getCaseSummaryListLA() {
        return defaultIfNull(caseSummaryListLA, new ArrayList<>());
    }

    public List<Element<CaseSummary>> getCaseSummaryListCTSC() {
        return defaultIfNull(caseSummaryListCTSC, new ArrayList<>());
    }

    public List<Element<PositionStatementChild>> getPosStmtChildList() {
        return defaultIfNull(posStmtChildList, new ArrayList<>());
    }

    public List<Element<PositionStatementChild>> getPosStmtChildListLA() {
        return defaultIfNull(posStmtChildListLA, new ArrayList<>());
    }

    public List<Element<PositionStatementChild>> getPosStmtChildListCTSC() {
        return defaultIfNull(posStmtChildListCTSC, new ArrayList<>());
    }

    public List<Element<PositionStatementRespondent>> getPosStmtRespList() {
        return defaultIfNull(posStmtRespList, new ArrayList<>());
    }

    public List<Element<PositionStatementRespondent>> getPosStmtRespListLA() {
        return defaultIfNull(posStmtRespListLA, new ArrayList<>());
    }

    public List<Element<PositionStatementRespondent>> getPosStmtRespListCTSC() {
        return defaultIfNull(posStmtRespListCTSC, new ArrayList<>());
    }

    public List<Element<SkeletonArgument>> getSkeletonArgumentList() {
        return defaultIfNull(skeletonArgumentList, new ArrayList<>());
    }

    public List<Element<SkeletonArgument>> getSkeletonArgumentListLA() {
        return defaultIfNull(skeletonArgumentListLA, new ArrayList<>());
    }

    public List<Element<SkeletonArgument>> getSkeletonArgumentListCTSC() {
        return defaultIfNull(skeletonArgumentListCTSC, new ArrayList<>());
    }

    public List<Element<ManagedDocument>> getPosStmtList() {
        return defaultIfNull(posStmtList, new ArrayList<>());
    }

    public List<Element<ManagedDocument>> getPosStmtListLA() {
        return defaultIfNull(posStmtListLA, new ArrayList<>());
    }

    public List<Element<ManagedDocument>> getPosStmtListCTSC() {
        return defaultIfNull(posStmtListCTSC, new ArrayList<>());
    }
}
