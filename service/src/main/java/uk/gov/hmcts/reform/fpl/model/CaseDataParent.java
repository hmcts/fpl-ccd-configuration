package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.model.CaseLocation;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ListingActionType;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.validation.groups.SecureAccommodationGroup;

import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORARPlus24RolesXmdwczAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CAFCASSSOLICITORRPlus10RolesOsrbexAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CAFCASSSOLICITORUPlus35RolesTjesuxAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERRuPlus39RolesPwbitcAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCrudPlus2RolesXzgsvpAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcassCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSuperuserCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperCrudPlus2RolesGjbeqhAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSolicitorCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcassCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERRPlus38RolesGedyfhAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGRPlus8RolesQbtompAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcassRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACruPlus28RolesNrpimkAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCruPlus7RolesYczlabAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERRSOLICITORCruCaseworkerPubliclawSystemupdateCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperRPlus3RolesWbcsaxAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERRSOLICITORCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACruPlus25RolesFufkkqAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERCrudPlus4RolesXbenugAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCrudPlus2RolesFhizhuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERRPlus40RolesJbdulqAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCruPlus2RolesBskdreAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawRparobotCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCrudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminUAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawGatekeeperRuPlus2RolesOrmxlgAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CAFCASSSOLICITORCaseworkerPubliclawCafcassCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCafcasssystemupdateRPlus1RolesQakbhsAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LABARRISTERCruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORBCuPlus23RolesLimnqvAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACruPlus5RolesUumdqfAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawSystemupdateCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORACrudPlus30RolesYejrpdAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CAFCASSSOLICITORCruPlus3RolesUpxliqAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.EPSMANAGINGCruLAMANAGINGCruLASHAREDCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CHILDSOLICITORASOLICITORACruAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.LASOLICITORCrudCaseworkerPubliclawSystemupdateCuAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.GSProfileRPlus42RolesKgccawAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminCudAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.fpl.ccd.access.CaseworkerPubliclawCourtadminLegalAdviserCrudAccess;

@JsonSubTypes({
    @JsonSubTypes.Type(value = CaseData.class)
})
@Jacksonized
@SuperBuilder(toBuilder = true)
@Data
public class CaseDataParent {

    @CCD(
            label = "Guardian Report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GuardianReport",
            access = {CHILDSOLICITORARPlus24RolesXmdwczAccess.class, CAFCASSSOLICITORRPlus10RolesOsrbexAccess.class}
    )
    protected final List<Element<ManagedDocument>> guardianReportsList;
    @CCD(
            label = "Guardian report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GuardianReportConfidential",
            access = {CHILDSOLICITORARPlus24RolesXmdwczAccess.class, CAFCASSSOLICITORRPlus10RolesOsrbexAccess.class}
    )
    protected final List<Element<GuardianReportConfidential>> guardianReportsListLA;
    @CCD(
            label = "Guardian report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GuardianReportConfidential",
            access = {CHILDSOLICITORARPlus24RolesXmdwczAccess.class, CAFCASSSOLICITORRPlus10RolesOsrbexAccess.class}
    )
    protected final List<Element<GuardianReportConfidential>> guardianReportsListCTSC;
    @CCD(
            label = "Guardian report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GuardianReport",
            access = {CAFCASSSOLICITORUPlus35RolesTjesuxAccess.class}
    )
    protected final List<Element<GuardianReport>> guardianReportsListRemoved;
    @CCD(label = "Respondent statements", searchable = false, access = {BARRISTERRuPlus39RolesPwbitcAccess.class})
    protected final List<Element<RespondentStatementV2>> respStmtList;
    @CCD(
            label = "Respondent statements",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentStatementV2Confidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class, LABARRISTERCrudPlus2RolesXzgsvpAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<RespondentStatementV2>> respStmtListLA;
    @CCD(
            label = "Respondent statements",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentStatementV2Confidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class, CaseworkerPubliclawSuperuserCrudAccess.class}
    )
    protected final List<Element<RespondentStatementV2>> respStmtListCTSC;
    @CCD(label = "Respondent statements", searchable = false)
    protected final List<Element<RespondentStatementV2>> respStmtListRemoved;
    @CCD(
            label = "For all parties",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<Direction>> allParties;
    @CCD(
            label = "Add directions",
            searchable = false,
            access = {CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, CaseworkerPubliclawGatekeeperCrudPlus2RolesGjbeqhAccess.class}
    )
    protected final List<Element<Direction>> allPartiesCustom;
    @CCD(
            label = "Local Authority Directions",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawSolicitorCrudAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<Direction>> localAuthorityDirections;
    @CCD(
            label = "Add directions",
            searchable = false,
            access = {CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, CaseworkerPubliclawGatekeeperCrudPlus2RolesGjbeqhAccess.class}
    )
    protected final List<Element<Direction>> localAuthorityDirectionsCustom;
    @CCD(
            label = "Court directions",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<Direction>> courtDirections;
    @CCD(
            label = "Add directions",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, CaseworkerPubliclawSolicitorCrudAccess.class}
    )
    protected final List<Element<Direction>> courtDirectionsCustom;
    @CCD(
            label = "Cafcass Directions",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawCafcassCrudAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<Direction>> cafcassDirections;
    @CCD(
            label = "Add directions",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, CaseworkerPubliclawSolicitorCrudAccess.class}
    )
    protected final List<Element<Direction>> cafcassDirectionsCustom;
    @CCD(
            label = "Other parties directions",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<Direction>> otherPartiesDirections;
    @CCD(
            label = "Add directions",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, CaseworkerPubliclawSolicitorCrudAccess.class}
    )
    protected final List<Element<Direction>> otherPartiesDirectionsCustom;
    @CCD(
            label = "Parents and respondents directions",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<Direction>> respondentDirections;
    @CCD(
            label = "Add directions",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, CaseworkerPubliclawSolicitorCrudAccess.class}
    )
    protected final List<Element<Direction>> respondentDirectionsCustom;
    @CCD(
            label = "Witness statements",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ApplicantWitnessStatement",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<ApplicantWitnessStatement>> applicantWitnessStmtList;
    @CCD(
            label = "Witness statements",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ApplicantWitnessStatementConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<ApplicantWitnessStatementConfidential>> applicantWitnessStmtListLA;
    @CCD(
            label = "Witness statements",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ApplicantWitnessStatementConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ApplicantWitnessStatementConfidential>> applicantWitnessStmtListCTSC;
    @CCD(
            label = "Witness statements",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ApplicantWitnessStatement",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ApplicantWitnessStatement>> applicantWitnessStmtListRemoved;
    @CCD(
            label = "Guardian's evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GuardianEvidence",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<GuardianEvidence>> guardianEvidenceList;
    @CCD(
            label = "Guardian's evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GuardianEvidenceConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<GuardianEvidenceConfidential>> guardianEvidenceListLA;
    @CCD(
            label = "Guardian's evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GuardianEvidenceConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<GuardianEvidenceConfidential>> guardianEvidenceListCTSC;
    @CCD(
            label = "Guardian's evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GuardianEvidence",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<GuardianEvidence>> guardianEvidenceListRemoved;
    @CCD(
            label = "Drug and alcohol reports",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DrugAndAlcoholReport",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<DrugAndAlcoholReport>> drugAndAlcoholReportList;
    @CCD(
            label = "Drug and alcohol reports",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DrugAndAlcoholReportConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<DrugAndAlcoholReportConfidential>> drugAndAlcoholReportListLA;
    @CCD(
            label = "Drug and alcohol reports",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DrugAndAlcoholReportConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<DrugAndAlcoholReportConfidential>> drugAndAlcoholReportListCTSC;
    @CCD(
            label = "Drug and alcohol reports",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DrugAndAlcoholReport",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<DrugAndAlcoholReport>> drugAndAlcoholReportListRemoved;
    @CCD(
            label = "Letters of instructions/referrals",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "LettersOfInstruction",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<LettersOfInstruction>> lettersOfInstructionList;
    @CCD(
            label = "Letters of instructions/referrals",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "LettersOfInstructionConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<LettersOfInstructionConfidential>> lettersOfInstructionListLA;
    @CCD(
            label = "Letters of instructions/referrals",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "LettersOfInstructionConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<LettersOfInstructionConfidential>> lettersOfInstructionListCTSC;
    @CCD(
            label = "Letters of instructions/referrals",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "LettersOfInstruction",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<LettersOfInstruction>> lettersOfInstructionListRemoved;
    @CCD(
            label = "Adult Psychiatric Report On Parent(s)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AdultPsychiatricReportOnParents",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<AdultPsychiatricReportOnParents>> adultPsychRepParentsList;
    @CCD(
            label = "Adult Psychiatric Report On Parent(s)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AdultPsychiatricReportOnParentsConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<AdultPsychiatricReportOnParentsConfidential>> adultPsychRepParentsListLA;
    @CCD(
            label = "Adult Psychiatric Report On Parent(s)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AdultPsychiatricReportOnParentsConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<AdultPsychiatricReportOnParentsConfidential>> adultPsychRepParentsListCTSC;
    @CCD(
            label = "Adult Psychiatric Report On Parent(s)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AdultPsychiatricReportOnParents",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<AdultPsychiatricReportOnParents>> adultPsychRepParentsListRemoved;
    @CCD(
            label = "Family Centre Assessments - Non-Residential",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FamilyCentreAssessmentsNonResidential",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<FamilyCentreAssessmentsNonResidential>> famCentreAssessNonResList;
    @CCD(
            label = "Family Centre Assessments - Non-Residential",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FamilyCentreAssessmentsNonResidentialConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<FamilyCentreAssessmentsNonResidentialConfidential>> famCentreAssessNonResListLA;
    @CCD(
            label = "Family Centre Assessments - Non-Residential",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FamilyCentreAssessmentsNonResidentialConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<FamilyCentreAssessmentsNonResidentialConfidential>> famCentreAssessNonResListCTSC;
    @CCD(
            label = "Family Centre Assessments - Non-Residential",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FamilyCentreAssessmentsNonResidential",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<FamilyCentreAssessmentsNonResidential>> famCentreAssessNonResListRemoved;
    @CCD(
            label = "Family Centre Assessments - Residential",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FamilyCentreAssessmentResidential",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<FamilyCentreAssessmentResidential>> familyCentreAssesResList;
    @CCD(
            label = "Family Centre Assessments - Residential",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FamilyCentreAssessmentResidentialConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<FamilyCentreAssessmentResidentialConfidential>> familyCentreAssesResListLA;
    @CCD(
            label = "Family Centre Assessments - Residential",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FamilyCentreAssessmentResidentialConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<FamilyCentreAssessmentResidentialConfidential>> familyCentreAssesResListCTSC;
    @CCD(
            label = "Family Centre Assessments - Residential",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FamilyCentreAssessmentResidential",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<FamilyCentreAssessmentResidential>> familyCentreAssesResListRemoved;
    @CCD(
            label = "Haematologist",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Haematologist",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<Haematologist>> haematologistList;
    @CCD(
            label = "Haematologist",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HaematologistConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<HaematologistConfidential>> haematologistListLA;
    @CCD(
            label = "Haematologist",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HaematologistConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<HaematologistConfidential>> haematologistListCTSC;
    @CCD(
            label = "Haematologist",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Haematologist",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<Haematologist>> haematologistListRemoved;
    @CCD(
            label = "Independent Social Worker",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "IndependentSocialWorker",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<IndependentSocialWorker>> indepSocialWorkerList;
    @CCD(
            label = "Independent Social Worker",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "IndependentSocialWorkerConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<IndependentSocialWorkerConfidential>> indepSocialWorkerListLA;
    @CCD(
            label = "Independent Social Worker",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "IndependentSocialWorkerConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<IndependentSocialWorkerConfidential>> indepSocialWorkerListCTSC;
    @CCD(
            label = "Independent Social Worker",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "IndependentSocialWorker",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<IndependentSocialWorker>> indepSocialWorkerListRemoved;
    @CCD(
            label = "Multi Disciplinary Assessment",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "MultiDisciplinaryAssessment",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<MultiDisciplinaryAssessment>> multiDisciplinAssessList;
    @CCD(
            label = "Multi Disciplinary Assessment",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "MultiDisciplinaryAssessmentConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<MultiDisciplinaryAssessmentConfidential>> multiDisciplinAssessListLA;
    @CCD(
            label = "Multi Disciplinary Assessment",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "MultiDisciplinaryAssessmentConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<MultiDisciplinaryAssessmentConfidential>> multiDisciplinAssessListCTSC;
    @CCD(
            label = "Multi Disciplinary Assessment",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "MultiDisciplinaryAssessment",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<MultiDisciplinaryAssessment>> multiDisciplinAssessListRemoved;
    @CCD(
            label = "Neurosurgeon",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NeuroSurgeon",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<NeuroSurgeon>> neuroSurgeonList;
    @CCD(
            label = "Neurosurgeon",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NeuroSurgeonConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<NeuroSurgeonConfidential>> neuroSurgeonListLA;
    @CCD(
            label = "Neurosurgeon",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NeuroSurgeonConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<NeuroSurgeonConfidential>> neuroSurgeonListCTSC;
    @CCD(
            label = "Neurosurgeon",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NeuroSurgeon",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<NeuroSurgeon>> neuroSurgeonListRemoved;
    @CCD(
            label = "Ophthalmologist",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Ophthalmologist",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<Ophthalmologist>> ophthalmologistList;
    @CCD(
            label = "Ophthalmologist",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OphthalmologistConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<OphthalmologistConfidential>> ophthalmologistListLA;
    @CCD(
            label = "Ophthalmologist",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OphthalmologistConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<OphthalmologistConfidential>> ophthalmologistListCTSC;
    @CCD(
            label = "Ophthalmologist",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Ophthalmologist",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<Ophthalmologist>> ophthalmologistListRemoved;
    @CCD(
            label = "Other Expert Report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OtherExpertReport",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<OtherExpertReport>> otherExpertReportList;
    @CCD(
            label = "Other Expert Report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OtherExpertReportConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<OtherExpertReportConfidential>> otherExpertReportListLA;
    @CCD(
            label = "Other Expert Report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OtherExpertReportConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<OtherExpertReportConfidential>> otherExpertReportListCTSC;
    @CCD(
            label = "Other Expert Report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OtherExpertReport",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<OtherExpertReport>> otherExpertReportListRemoved;
    @CCD(
            label = "Other Medical Report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OtherMedicalReport",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<OtherMedicalReport>> otherMedicalReportList;
    @CCD(
            label = "Other Medical Report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OtherMedicalReportConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<OtherMedicalReportConfidential>> otherMedicalReportListLA;
    @CCD(
            label = "Other Medical Report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OtherMedicalReportConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<OtherMedicalReportConfidential>> otherMedicalReportListCTSC;
    @CCD(
            label = "Other Medical Report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "OtherMedicalReport",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<OtherMedicalReport>> otherMedicalReportListRemoved;
    @CCD(
            label = "Pediatric",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Pediatric",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<Pediatric>> pediatricList;
    @CCD(
            label = "Pediatric",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PediatricConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<PediatricConfidential>> pediatricListLA;
    @CCD(
            label = "Pediatric",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PediatricConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PediatricConfidential>> pediatricListCTSC;
    @CCD(
            label = "Pediatric",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Pediatric",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<Pediatric>> pediatricListRemoved;
    @CCD(
            label = "Pediatric Radiologist",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PediatricRadiologist",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<PediatricRadiologist>> pediatricRadiologistList;
    @CCD(
            label = "Pediatric Radiologist",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PediatricRadiologistConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<PediatricRadiologistConfidential>> pediatricRadiologistListLA;
    @CCD(
            label = "Pediatric Radiologist",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PediatricRadiologistConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PediatricRadiologistConfidential>> pediatricRadiologistListCTSC;
    @CCD(
            label = "Pediatric Radiologist",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PediatricRadiologist",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PediatricRadiologist>> pediatricRadiologistListRemoved;
    @CCD(
            label = "Professional: DNA Testing",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalDNATesting",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<ProfessionalDNATesting>> profDNATestingList;
    @CCD(
            label = "Professional: DNA Testing",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalDNATestingConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<ProfessionalDNATestingConfidential>> profDNATestingListLA;
    @CCD(
            label = "Professional: DNA Testing",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalDNATestingConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ProfessionalDNATestingConfidential>> profDNATestingListCTSC;
    @CCD(
            label = "Professional: DNA Testing",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalDNATesting",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ProfessionalDNATesting>> profDNATestingListRemoved;
    @CCD(
            label = "Professional: Drug/Alcohol",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalDrugAndAlcohol",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<ProfessionalDrugAndAlcohol>> profDrugAlcoholList;
    @CCD(
            label = "Professional: Drug/Alcohol",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalDrugAndAlcoholConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<ProfessionalDrugAndAlcoholConfidential>> profDrugAlcoholListLA;
    @CCD(
            label = "Professional: Drug/Alcohol",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalDrugAndAlcoholConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ProfessionalDrugAndAlcoholConfidential>> profDrugAlcoholListCTSC;
    @CCD(
            label = "Professional: Drug/Alcohol",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalDrugAndAlcohol",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ProfessionalDrugAndAlcohol>> profDrugAlcoholListRemoved;
    @CCD(
            label = "Professional: Hair Strand",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalHairStrand",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<ProfessionalHairStrand>> professionalHairStrandList;
    @CCD(
            label = "Professional: Hair Strand",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalHairStrandConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<ProfessionalHairStrandConfidential>> professionalHairStrandListLA;
    @CCD(
            label = "Professional: Hair Strand",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalHairStrandConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ProfessionalHairStrandConfidential>> professionalHairStrandListCTSC;
    @CCD(
            label = "Professional: Hair Strand",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalHairStrand",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ProfessionalHairStrand>> professionalHairStrandListRemoved;
    @CCD(
            label = "Professional: Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalOther",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<ProfessionalOther>> professionalOtherList;
    @CCD(
            label = "Professional: Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalOtherConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<ProfessionalOtherConfidential>> professionalOtherListLA;
    @CCD(
            label = "Professional: Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalOtherConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ProfessionalOtherConfidential>> professionalOtherListCTSC;
    @CCD(
            label = "Professional: Other",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ProfessionalOther",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ProfessionalOther>> professionalOtherListRemoved;
    @CCD(
            label = "Psychiatric - On child only",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychiatricChildOnly",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<PsychiatricChildOnly>> psychiatricChildOnlyList;
    @CCD(
            label = "Psychiatric - On child only",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychiatricChildOnlyConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<PsychiatricChildOnlyConfidential>> psychiatricChildOnlyListLA;
    @CCD(
            label = "Psychiatric - On child only",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychiatricChildOnlyConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychiatricChildOnlyConfidential>> psychiatricChildOnlyListCTSC;
    @CCD(
            label = "Psychiatric - On child only",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychiatricChildOnly",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychiatricChildOnly>> psychiatricChildOnlyListRemoved;
    @CCD(
            label = "Psychiatric - On child and Parent(s)/carers",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychiatricOnChildAndParentCarers",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassRAccess.class}
    )
    protected final List<Element<PsychiatricOnChildAndParentCarers>> psychChildParentCarersList;
    @CCD(
            label = "Psychiatric - On child and Parent(s)/carers",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychiatricOnChildAndParentCarersConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<PsychiatricOnChildAndParentCarersConfidential>> psychChildParentCarersListLA;
    @CCD(
            label = "Psychiatric - On child and Parent(s)/carers",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychiatricOnChildAndParentCarersConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychiatricOnChildAndParentCarersConfidential>> psychChildParentCarersListCTSC;
    @CCD(
            label = "Psychiatric - On child and Parent(s)/carers",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychiatricOnChildAndParentCarers",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychiatricOnChildAndParentCarers>> psychChildParentCarersListRemoved;
    @CCD(
            label = "Psychological Report on Child Only - Clinical",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnChildOnlyClinical",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<PsychologicalReportOnChildOnlyClinical>> psycReportChildClinList;
    @CCD(
            label = "Psychological Report on Child Only - Clinical",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnChildOnlyClinicalConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<PsychologicalReportOnChildOnlyClinicalConfidential>> psycReportChildClinListLA;
    @CCD(
            label = "Psychological Report on Child Only - Clinical",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnChildOnlyClinicalConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychologicalReportOnChildOnlyClinicalConfidential>> psycReportChildClinListCTSC;
    @CCD(
            label = "Psychological Report on Child Only - Clinical",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnChildOnlyClinical",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychologicalReportOnChildOnlyClinical>> psycReportChildClinListRemoved;
    @CCD(
            label = "Psychological Report on Child Only - Educational",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnChildOnlyEducational",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<PsychologicalReportOnChildOnlyEducational>> psycReportChildOnlyEdList;
    @CCD(
            label = "Psychological Report on Child Only - Educational",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnChildOnlyEducationalConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<PsychologicalReportOnChildOnlyEducationalConfidential>> psycReportChildOnlyEdListLA;
    @CCD(
            label = "Psychological Report on Child Only - Educational",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnChildOnlyEducationalConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychologicalReportOnChildOnlyEducationalConfidential>> psycReportChildOnlyEdListCTSC;
    @CCD(
            label = "Psychological Report on Child Only - Educational",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnChildOnlyEducational",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychologicalReportOnChildOnlyEducational>> psycReportChildOnlyEdListRemoved;
    @CCD(
            label = "Psychological Report on Parent(s) and child",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnParentAndChild",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<PsychologicalReportOnParentAndChild>> psychReportParentChildList;
    @CCD(
            label = "Psychological Report on Parent(s) and child",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnParentAndChildConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<PsychologicalReportOnParentAndChildConfidential>> psychReportParentChildListLA;
    @CCD(
            label = "Psychological Report on Parent(s) and child",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnParentAndChildConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychologicalReportOnParentAndChildConfidential>> psychReportParentChildListCTSC;
    @CCD(
            label = "Psychological Report on Parent(s) and child",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnParentAndChild",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychologicalReportOnParentAndChild>> psychReportParentChildListRemoved;
    @CCD(
            label = "Psychological Report on Parent(s) - full cognitive",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnParentFullCognitive",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<PsychologicalReportOnParentFullCognitive>> psychRepParentFullCogList;
    @CCD(
            label = "Psychological Report on Parent(s) - full cognitive",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnParentFullCognitiveConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<PsychologicalReportOnParentFullCognitiveConfidential>> psychRepParentFullCogListLA;
    @CCD(
            label = "Psychological Report on Parent(s) - full cognitive",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnParentFullCognitiveConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychologicalReportOnParentFullCognitiveConfidential>> psychRepParentFullCogListCTSC;
    @CCD(
            label = "Psychological Report on Parent(s) - full cognitive",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnParentFullCognitive",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychologicalReportOnParentFullCognitive>> psychRepParentFullCogListRemoved;
    @CCD(
            label = "Psychological Report on Parent(s) - functioning",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnParentFunctioning",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<PsychologicalReportOnParentFunctioning>> psychRepParentFuncList;
    @CCD(
            label = "Psychological Report on Parent(s) - functioning",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnParentFunctioningConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<PsychologicalReportOnParentFunctioningConfidential>> psychRepParentFuncListLA;
    @CCD(
            label = "Psychological Report on Parent(s) - functioning",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnParentFunctioningConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychologicalReportOnParentFunctioningConfidential>> psychRepParentFuncListCTSC;
    @CCD(
            label = "Psychological Report on Parent(s) - functioning",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PsychologicalReportOnParentFunctioning",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PsychologicalReportOnParentFunctioning>> psychRepParentFuncListRemoved;
    @CCD(
            label = "Toxicology report/statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ToxicologyReportStatement",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<ToxicologyReportStatement>> toxicologyStatementList;
    @CCD(
            label = "Toxicology report/statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ToxicologyReportStatementConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<ToxicologyReportStatementConfidential>> toxicologyStatementListLA;
    @CCD(
            label = "Toxicology report/statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ToxicologyReportStatementConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ToxicologyReportStatementConfidential>> toxicologyStatementListCTSC;
    @CCD(
            label = "Toxicology report/statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ToxicologyReportStatement",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ToxicologyReportStatement>> toxicologyStatementListRemoved;
    @CCD(
            label = "Expert reports",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ExpertReport",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<ExpertReport>> expertReportList;
    @CCD(
            label = "Expert reports",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ExpertReportConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<ExpertReportConfidential>> expertReportListLA;
    @CCD(
            label = "Expert reports",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ExpertReportConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ExpertReportConfidential>> expertReportListCTSC;
    @CCD(
            label = "Expert reports",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ExpertReport",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<ExpertReport>> expertReportListRemoved;
    @CCD(
            label = "Police disclosure",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PoliceDisclosure",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<PoliceDisclosure>> policeDisclosureList;
    @CCD(
            label = "Police disclosure",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PoliceDisclosureConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<PoliceDisclosureConfidential>> policeDisclosureListLA;
    @CCD(
            label = "Police disclosure",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PoliceDisclosureConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PoliceDisclosureConfidential>> policeDisclosureListCTSC;
    @CCD(
            label = "Police disclosure",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PoliceDisclosure",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PoliceDisclosure>> policeDisclosureListRemoved;
    @CCD(
            label = "Medical Records",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "MedicalRecord",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<MedicalRecord>> medicalRecordList;
    @CCD(
            label = "Medical Records",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "MedicalRecordConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<MedicalRecordConfidential>> medicalRecordListLA;
    @CCD(
            label = "Medical Records",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "MedicalRecordConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<MedicalRecordConfidential>> medicalRecordListCTSC;
    @CCD(
            label = "Medical Records",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "MedicalRecord",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<MedicalRecord>> medicalRecordListRemoved;
    @CCD(
            label = "Notice of acting / Notice of issue",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NoticeOfActingOrIssue",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<NoticeOfActingOrIssue>> noticeOfActingOrIssueList;
    @CCD(
            label = "Notice of acting / Notice of issue",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NoticeOfActingOrIssue",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<NoticeOfActingOrIssue>> noticeOfActingOrIssueListLA;
    @CCD(
            label = "Notice of acting / Notice of issue",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NoticeOfActingOrIssue",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<NoticeOfActingOrIssue>> noticeOfActingOrIssueListCTSC;
    @CCD(
            label = "Notice of acting / Notice of issue",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NoticeOfActingOrIssue",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<NoticeOfActingOrIssue>> noticeOfActingOrIssueListRemoved;

    @CCD(
            label = "Parent assessments",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ParentAssessment",
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, LABARRISTERCruPlus7RolesYczlabAccess.class, BARRISTERRSOLICITORCruCaseworkerPubliclawSystemupdateCruAccess.class}
    )
    protected final List<Element<ParentAssessment>> parentAssessmentList;
    @CCD(
            label = "Parent assessments",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ParentAssessmentConfidential",
            access = {LABARRISTERCruPlus7RolesYczlabAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawSystemupdateCruAccess.class}
    )
    protected final List<Element<ParentAssessmentConfidential>> parentAssessmentListLA;
    @CCD(
            label = "Parent assessments",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ParentAssessmentConfidential",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawGatekeeperRPlus3RolesWbcsaxAccess.class}
    )
    protected final List<Element<ParentAssessmentConfidential>> parentAssessmentListCTSC;
    @CCD(
            label = "Parent assessments",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ParentAssessment"
    )
    protected final List<Element<ParentAssessment>> parentAssessmentListRemoved;
    @CCD(
            label = "Family and viability assessments",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FamilyAndViabilityAssessment",
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, LABARRISTERCruPlus7RolesYczlabAccess.class, BARRISTERRSOLICITORCruCaseworkerPubliclawSystemupdateCruAccess.class}
    )
    protected final List<Element<FamilyAndViabilityAssessment>> famAndViabilityList;
    @CCD(
            label = "Family and viability assessments",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FamilyAndViabilityAssessmentConfidential",
            access = {LABARRISTERCruPlus7RolesYczlabAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawSystemupdateCruAccess.class}
    )
    protected final List<Element<FamilyAndViabilityAssessmentConfidential>> famAndViabilityListLA;
    @CCD(
            label = "Family and viability assessments",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FamilyAndViabilityAssessmentConfidential",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawGatekeeperRPlus3RolesWbcsaxAccess.class}
    )
    protected final List<Element<FamilyAndViabilityAssessmentConfidential>> famAndViabilityListCTSC;
    @CCD(
            label = "Family and viability assessments",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FamilyAndViabilityAssessment"
    )
    protected final List<Element<FamilyAndViabilityAssessment>> famAndViabilityListRemoved;
    @CCD(
            label = "Applicant's other documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ApplicantsOtherDocument",
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, LABARRISTERCruPlus7RolesYczlabAccess.class, BARRISTERRSOLICITORCruCaseworkerPubliclawSystemupdateCruAccess.class}
    )
    protected final List<Element<ApplicantsOtherDocument>> applicantOtherDocList;
    @CCD(
            label = "Applicant's other documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ApplicantsOtherDocumentConfidential",
            access = {LABARRISTERCruPlus7RolesYczlabAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawSystemupdateCruAccess.class}
    )
    protected final List<Element<ApplicantsOtherDocumentConfidential>> applicantOtherDocListLA;
    @CCD(
            label = "Applicant's other documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ApplicantsOtherDocumentConfidential",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawGatekeeperRPlus3RolesWbcsaxAccess.class}
    )
    protected final List<Element<ApplicantsOtherDocumentConfidential>> applicantOtherDocListCTSC;
    @CCD(
            label = "Applicant's other documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ApplicantsOtherDocument"
    )
    protected final List<Element<ApplicantsOtherDocument>> applicantOtherDocListRemoved;
    @CCD(
            label = "Meeting notes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "MeetingNote",
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, LABARRISTERCruPlus7RolesYczlabAccess.class, BARRISTERRSOLICITORCruCaseworkerPubliclawSystemupdateCruAccess.class}
    )
    protected final List<Element<MeetingNote>> meetingNoteList;
    @CCD(
            label = "Meeting notes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "MeetingNoteConfidential",
            access = {LABARRISTERCruPlus7RolesYczlabAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawSystemupdateCruAccess.class}
    )
    protected final List<Element<MeetingNoteConfidential>> meetingNoteListLA;
    @CCD(
            label = "Meeting notes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "MeetingNoteConfidential",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawGatekeeperRPlus3RolesWbcsaxAccess.class}
    )
    protected final List<Element<MeetingNoteConfidential>> meetingNoteListCTSC;
    @CCD(
            label = "Meeting notes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "MeetingNote"
    )
    protected final List<Element<MeetingNote>> meetingNoteListRemoved;
    @CCD(
            label = "Contact notes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ContactNote",
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, LABARRISTERCruPlus7RolesYczlabAccess.class, BARRISTERRSOLICITORCruCaseworkerPubliclawSystemupdateCruAccess.class}
    )
    protected final List<Element<ContactNote>> contactNoteList;
    @CCD(
            label = "Contact notes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ContactNoteConfidential",
            access = {LABARRISTERCruPlus7RolesYczlabAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawSystemupdateCruAccess.class}
    )
    protected final List<Element<ContactNoteConfidential>> contactNoteListLA;
    @CCD(
            label = "Contact notes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ContactNoteConfidential",
            access = {CaseworkerPubliclawCourtadminCaseworkerPubliclawSystemupdateCruAccess.class, CaseworkerPubliclawGatekeeperRPlus3RolesWbcsaxAccess.class}
    )
    protected final List<Element<ContactNoteConfidential>> contactNoteListCTSC;
    @CCD(
            label = "Contact notes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ContactNote"
    )
    protected final List<Element<ContactNote>> contactNoteListRemoved;
    @CCD(
            label = "Judgments/facts and reasons",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "JudgementFactsAndReasons",
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, LABARRISTERCruPlus7RolesYczlabAccess.class, BARRISTERRSOLICITORCruAccess.class}
    )
    protected final List<Element<JudgementFactsAndReasons>> judgementList;
    @CCD(
            label = "Judgments/facts and reasons",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "JudgementFactsAndReasons",
            access = {LABARRISTERCruPlus7RolesYczlabAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class}
    )
    protected final List<Element<JudgementFactsAndReasons>> judgementListLA;
    @CCD(
            label = "Judgments/facts and reasons",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "JudgementFactsAndReasons",
            access = {CaseworkerPubliclawGatekeeperRPlus3RolesWbcsaxAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    protected final List<Element<JudgementFactsAndReasons>> judgementListCTSC;
    @CCD(
            label = "Judgments/facts and reasons",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "JudgementFactsAndReasons"
    )
    protected final List<Element<JudgementFactsAndReasons>> judgementListRemoved;
    @CCD(
            label = "Transcripts",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Transcript",
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, LABARRISTERCruPlus7RolesYczlabAccess.class, BARRISTERRSOLICITORCruAccess.class}
    )
    protected final List<Element<Transcript>> transcriptList;
    @CCD(
            label = "Transcripts",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "TranscriptConfidential",
            access = {LABARRISTERCruPlus7RolesYczlabAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class}
    )
    protected final List<Element<TranscriptConfidential>> transcriptListLA;
    @CCD(
            label = "Transcripts",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "TranscriptConfidential",
            access = {CaseworkerPubliclawGatekeeperRPlus3RolesWbcsaxAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    protected final List<Element<TranscriptConfidential>> transcriptListCTSC;
    @CCD(
            label = "Transcripts",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Transcript"
    )
    protected final List<Element<Transcript>> transcriptListRemoved;
    @CCD(
            label = "Witness statements",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentWitnessStatement",
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, LABARRISTERCruPlus7RolesYczlabAccess.class, BARRISTERRSOLICITORCruAccess.class}
    )
    protected final List<Element<RespondentWitnessStatement>> respWitnessStmtList;
    @CCD(
            label = "Witness statements",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentWitnessStatementConfidential",
            access = {LABARRISTERCruPlus7RolesYczlabAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class}
    )
    protected final List<Element<RespondentWitnessStatementConfidential>> respWitnessStmtListLA;
    @CCD(
            label = "Witness statements",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentWitnessStatementConfidential",
            access = {CaseworkerPubliclawGatekeeperRPlus3RolesWbcsaxAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    protected final List<Element<RespondentWitnessStatementConfidential>> respWitnessStmtListCTSC;
    @CCD(
            label = "Witness statements",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentWitnessStatement"
    )
    protected final List<Element<RespondentWitnessStatement>> respWitnessStmtListRemoved;
    @CCD(
            label = "Previous Proceedings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PreviousProceeding",
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, LABARRISTERCruPlus7RolesYczlabAccess.class, BARRISTERRSOLICITORCruAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PreviousProceeding>> previousProceedingList;
    @CCD(
            label = "Previous Proceedings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PreviousProceedingConfidential",
            access = {LABARRISTERCruPlus7RolesYczlabAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PreviousProceedingConfidential>> previousProceedingListLA;
    @CCD(
            label = "Previous Proceedings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PreviousProceedingConfidential",
            access = {CaseworkerPubliclawGatekeeperRPlus3RolesWbcsaxAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class}
    )
    protected final List<Element<PreviousProceedingConfidential>> previousProceedingListCTSC;
    @CCD(
            label = "Previous Proceedings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PreviousProceeding",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<PreviousProceeding>> previousProceedingListRemoved;
    @CCD(
            label = "Threshold",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Threshold",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CHILDSOLICITORACruPlus25RolesFufkkqAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, BARRISTERCrudPlus4RolesXbenugAccess.class}
    )
    protected final List<Element<ManagedDocument>> thresholdList;
    @CCD(
            label = "Threshold",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ThresholdConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, LABARRISTERCrudPlus2RolesFhizhuAccess.class}
    )
    protected final List<Element<ManagedDocument>> thresholdListLA;
    @CCD(
            label = "Threshold",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ThresholdConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class}
    )
    protected final List<Element<ThresholdConfidential>> thresholdListCTSC;
    @CCD(
            label = "Threshold",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Threshold"
    )
    protected final List<Element<Threshold>> thresholdListRemoved;
    @CCD(
            label = "Documents filed on issue",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentsFiledOnIssue",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CHILDSOLICITORACruPlus25RolesFufkkqAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, BARRISTERCrudPlus4RolesXbenugAccess.class}
    )
    protected final List<Element<ManagedDocument>> documentsFiledOnIssueList;
    @CCD(
            label = "Documents filed on issue",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentsFiledOnIssueConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, LABARRISTERCrudPlus2RolesFhizhuAccess.class}
    )
    protected final List<Element<ManagedDocument>> documentsFiledOnIssueListLA;
    @CCD(
            label = "Documents filed on issue",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentsFiledOnIssueConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class}
    )
    protected final List<Element<DocumentsFiledOnIssueConfidential>> documentsFiledOnIssueListCTSC;
    @CCD(
            label = "Documents filed on issue",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentsFiledOnIssue"
    )
    protected final List<Element<DocumentsFiledOnIssue>> documentsFiledOnIssueListRemoved;
    @CCD(
            label = "Care plan",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CarePlan",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CHILDSOLICITORACruPlus25RolesFufkkqAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, BARRISTERCrudPlus4RolesXbenugAccess.class}
    )
    protected final List<Element<ManagedDocument>> carePlanList;
    @CCD(
            label = "Care plan",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CarePlanConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCrudAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class, LABARRISTERCrudPlus2RolesFhizhuAccess.class}
    )
    protected final List<Element<ManagedDocument>> carePlanListLA;
    @CCD(
            label = "Care plan",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CarePlanConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateRPlus1RolesJpsblzAccess.class}
    )
    protected final List<Element<CarePlanConfidential>> carePlanListCTSC;
    @CCD(
            label = "Care plan",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CarePlan"
    )
    protected final List<Element<CarePlan>> carePlanListRemoved;

    @CCD(
            label = "Correspondence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CorrespondenceDocument",
            access = {BARRISTERRPlus40RolesJbdulqAccess.class}
    )
    protected final List<Element<CorrespondenceDocument>> correspondenceDocList;
    @CCD(
            label = "Correspondence (Confidential)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CorrespondenceDocumentConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORRAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class, LABARRISTERCruPlus2RolesBskdreAccess.class, CaseworkerPubliclawRparobotCruAccess.class}
    )
    protected final List<Element<CorrespondenceDocumentConfidential>> correspondenceDocListLA;
    @CCD(
            label = "Correspondence (Restricted to CTSC only)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CorrespondenceDocumentConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class, CaseworkerPubliclawRparobotCruAccess.class}
    )
    protected final List<Element<CorrespondenceDocumentConfidential>> correspondenceDocListCTSC;
    @CCD(
            label = "Correspondence (Removed)",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CorrespondenceDocument"
    )
    protected final List<Element<CorrespondenceDocument>> correspondenceDocListRemoved;

    @CCD(
            label = "Archived migrated data",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ArchivedDocument",
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    protected final List<Element<ArchivedDocument>> archivedDocumentsList;
    @CCD(
            label = "Archived migrated data",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ArchivedDocument",
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    protected final List<Element<ArchivedDocument>> archivedDocumentsListLA;
    @CCD(
            label = "Archived migrated data",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ArchivedDocument",
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    protected final List<Element<ArchivedDocument>> archivedDocumentsListCTSC;
    @CCD(
            label = "Archived migrated data",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ArchivedDocument",
            access = {CaseworkerPubliclawCourtadminUAccess.class}
    )
    protected final List<Element<ArchivedDocument>> archivedDocumentsListRemoved;


    @CCD(
            label = "C1 Application Document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "C1ApplicationDocument",
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CaseworkerPubliclawGatekeeperRuPlus2RolesOrmxlgAccess.class, CAFCASSSOLICITORCaseworkerPubliclawCafcassCruAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesQakbhsAccess.class}
    )
    protected final List<Element<C1ApplicationDocument>> c1ApplicationDocList;
    @CCD(
            label = "C1 Application Document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "C1ApplicationDocumentConfidential",
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class, CaseworkerPubliclawGatekeeperRuPlus2RolesOrmxlgAccess.class, LABARRISTERCruAccess.class}
    )
    protected final List<Element<C1ApplicationDocumentConfidential>> c1ApplicationDocListLA;
    @CCD(
            label = "C1 Application Document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "C1ApplicationDocumentConfidential",
            access = {CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class, CaseworkerPubliclawGatekeeperRuPlus2RolesOrmxlgAccess.class}
    )
    protected final List<Element<C1ApplicationDocumentConfidential>> c1ApplicationDocListCTSC;
    @CCD(
            label = "C1 Application Document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "C1ApplicationDocument"
    )
    protected final List<Element<C1ApplicationDocument>> c1ApplicationDocListRemoved;
    @CCD(
            label = "C2 Application Document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "C2ApplicationDocument",
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, BARRISTERLABARRISTERSOLICITORCaseworkerPubliclawCourtadminCruAccess.class, CaseworkerPubliclawGatekeeperRuPlus2RolesOrmxlgAccess.class, CAFCASSSOLICITORCaseworkerPubliclawCafcassCruAccess.class, CaseworkerPubliclawCafcasssystemupdateRPlus1RolesQakbhsAccess.class}
    )
    protected final List<Element<C2ApplicationDocument>> c2ApplicationDocList;
    @CCD(
            label = "C2 Application Document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "C2ApplicationDocumentConfidential",
            access = {EPSMANAGINGLAMANAGINGLASHAREDLASOLICITORCruAccess.class, CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class, CaseworkerPubliclawGatekeeperRuPlus2RolesOrmxlgAccess.class, LABARRISTERCruAccess.class}
    )
    protected final List<Element<C2ApplicationDocumentConfidential>> c2ApplicationDocListLA;
    @CCD(
            label = "C2 Application Document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "C2ApplicationDocumentConfidential",
            access = {CaseworkerPubliclawCourtadminCruPlus1RolesOtdddfAccess.class, CaseworkerPubliclawGatekeeperRuPlus2RolesOrmxlgAccess.class}
    )
    protected final List<Element<C2ApplicationDocumentConfidential>> c2ApplicationDocListCTSC;
    @CCD(
            label = "C2 Application Document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "C2ApplicationDocument"
    )
    protected final List<Element<C2ApplicationDocument>> c2ApplicationDocListRemoved;

    @CCD(
            label = "Advocate Meeting Minutes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AdvocateMeetingMinute",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<AdvocateMeetingMinute>> advocateMeetingMinuteList;
    @CCD(
            label = "Advocate Meeting Minutes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AdvocateMeetingMinuteConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<AdvocateMeetingMinuteConfidential>> advocateMeetingMinuteListLA;
    @CCD(
            label = "Advocate Meeting Minutes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AdvocateMeetingMinuteConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<AdvocateMeetingMinuteConfidential>> advocateMeetingMinuteListCTSC;
    @CCD(
            label = "Advocate Meeting Minutes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AdvocateMeetingMinuteConfidential",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<AdvocateMeetingMinuteConfidential>> advocateMeetingMinuteListRemoved;

    @CCD(
            label = "Witness Template",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "WitnessTemplate",
            access = {BARRISTERRPlus38RolesGedyfhAccess.class, CaseworkerPubliclawCafcassCruAccess.class}
    )
    protected final List<Element<WitnessTemplate>> witnessTemplateList;
    @CCD(
            label = "Witness Template",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "WitnessTemplateConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, EPSMANAGINGRPlus8RolesQbtompAccess.class}
    )
    protected final List<Element<WitnessTemplateConfidential>> witnessTemplateListLA;
    @CCD(
            label = "Witness Template",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "WitnessTemplateConfidential",
            access = {CaseworkerPubliclawCourtadminCrudPlus2RolesEssjlqAccess.class, CaseworkerPubliclawMagistrateCaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<WitnessTemplateConfidential>> witnessTemplateListCTSC;
    @CCD(
            label = "Witness Template",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "WitnessTemplateConfidential",
            access = {CaseworkerPubliclawSystemupdateCrudAccess.class}
    )
    protected final List<Element<WitnessTemplateConfidential>> witnessTemplateListRemoved;

    @CCD(
            label = "How are there grounds for a secure accommodation order?",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, CHILDSOLICITORACruPlus5RolesUumdqfAccess.class, CaseworkerPubliclawSystemupdateCuAccess.class}
    )
    @NotNull(message = "Add the grounds for the application", groups = SecureAccommodationGroup.class)
    @Valid
    protected final GroundsForSecureAccommodationOrder groundsForSecureAccommodationOrder;

    @CCD(
            label = "How are there grounds for an authority to refuse contact with a child in care application?",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, CHILDSOLICITORACruPlus5RolesUumdqfAccess.class, CaseworkerPubliclawSystemupdateCuAccess.class}
    )
    @NotNull(message = "Add the grounds for the application")
    @Valid
    protected final GroundsForRefuseContactWithChild groundsForRefuseContactWithChild;

    @CCD(
            label = "How are there grounds for a child recovery order?",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, CHILDSOLICITORACruPlus5RolesUumdqfAccess.class, CaseworkerPubliclawSystemupdateCuAccess.class}
    )
    @NotNull(message = "Add the grounds for the application")
    @Valid
    protected final GroundsForChildRecoveryOrder groundsForChildRecoveryOrder;

    @CCD(
            label = "How are there grounds for a contact with a child in care application?",
            searchable = false,
            access = {CHILDSOLICITORACrudPlus30RolesYejrpdAccess.class}
    )
    @NotNull(message = "Add the grounds for the application")
    @Valid
    protected final GroundsForContactWithChild groundsForContactWithChild;

    @CCD(
            label = "Skip payment page",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CHILDSOLICITORACruPlus28RolesNrpimkAccess.class, CAFCASSSOLICITORCruPlus3RolesUpxliqAccess.class, CaseworkerPubliclawCourtadminCruAccess.class}
    )
    protected final YesNo skipPaymentPage;

    @CCD(
            label = "How are there grounds for an education supervision order?",
            searchable = false,
            access = {CHILDSOLICITORBCuPlus23RolesLimnqvAccess.class, EPSMANAGINGCruLAMANAGINGCruLASHAREDCuAccess.class, CHILDSOLICITORASOLICITORACruAccess.class, LASOLICITORCrudCaseworkerPubliclawSystemupdateCuAccess.class}
    )
    @NotNull(message = "Add the grounds for the application")
    @Valid
    protected final GroundsForEducationSupervisionOrder groundsForEducationSupervisionOrder;

    @CCD(
            label = "Would you like to remind the applicant to upload the CMO for these hearings?",
            searchable = false,
            typeParameterOverride = "YesOrNoFixedList",
            typeParameterClass = YesOrNoFixedList.class,
            access = {CaseworkerPubliclawCourtadminCruAccess.class}
    )
    protected final YesNo shouldSendOrderReminder;

    @CCD(label = " ", access = {GSProfileRPlus42RolesKgccawAccess.class})
    protected final CaseLocation caseManagementLocation;
    @CCD(
            label = "Listing request",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCudAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    protected final List<Element<ListingActionRequest>> listingRequests;
    @CCD(
            label = "Listing action request",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminLegalAdviserCrudAccess.class}
    )
    protected final ListingActionRequest listingRequestToReview;
    @CCD(
            label = "Reviewed listing requests",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCudAccess.class}
    )
    protected final List<Element<ListingActionRequest>> reviewedListingRequests;
    @CCD(
            label = "Which listing actions are required?",
            hint = "Select all that apply",
            searchable = false,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    protected final List<ListingActionType> selectListingActions;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPubliclawCourtadminCrudAccess.class}
    )
    protected final String listingDetails;
    @CCD(
            label = "Select request to review",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPubliclawCourtadminLegalAdviserCrudAccess.class}
    )
    protected final DynamicList listingRequestsList;

    public List<Element<ManagedDocument>> getGuardianReportsList() {
        return guardianReportsList != null ? guardianReportsList : new ArrayList<>();
    }
}
