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

@JsonSubTypes({
    @JsonSubTypes.Type(value = CaseData.class)
})
@Jacksonized
@SuperBuilder(toBuilder = true)
@Data
public class CaseDataParent {

    protected final List<Element<ManagedDocument>> guardianReportsList;
    protected final List<Element<ManagedDocument>> guardianReportsListLA;
    protected final List<Element<ManagedDocument>> guardianReportsListCTSC;
    protected final List<Element<ManagedDocument>> guardianReportsListRemoved;
    protected final List<Element<RespondentStatementV2>> respStmtList;
    protected final List<Element<RespondentStatementV2>> respStmtListLA;
    protected final List<Element<RespondentStatementV2>> respStmtListCTSC;
    protected final List<Element<RespondentStatementV2>> respStmtListRemoved;
    protected final List<Element<Direction>> allParties;
    protected final List<Element<Direction>> allPartiesCustom;
    protected final List<Element<Direction>> localAuthorityDirections;
    protected final List<Element<Direction>> localAuthorityDirectionsCustom;
    protected final List<Element<Direction>> courtDirections;
    protected final List<Element<Direction>> courtDirectionsCustom;
    protected final List<Element<Direction>> cafcassDirections;
    protected final List<Element<Direction>> cafcassDirectionsCustom;
    protected final List<Element<Direction>> otherPartiesDirections;
    protected final List<Element<Direction>> otherPartiesDirectionsCustom;
    protected final List<Element<Direction>> respondentDirections;
    protected final List<Element<Direction>> respondentDirectionsCustom;
    protected final List<Element<ManagedDocument>> applicantWitnessStmtList;
    protected final List<Element<ManagedDocument>> applicantWitnessStmtListLA;
    protected final List<Element<ManagedDocument>> applicantWitnessStmtListCTSC;
    protected final List<Element<ManagedDocument>> applicantWitnessStmtListRemoved;
    protected final List<Element<ManagedDocument>> guardianEvidenceList;
    protected final List<Element<ManagedDocument>> guardianEvidenceListLA;
    protected final List<Element<ManagedDocument>> guardianEvidenceListCTSC;
    protected final List<Element<ManagedDocument>> guardianEvidenceListRemoved;
    protected final List<Element<ManagedDocument>> drugAndAlcoholReportList;
    protected final List<Element<ManagedDocument>> drugAndAlcoholReportListLA;
    protected final List<Element<ManagedDocument>> drugAndAlcoholReportListCTSC;
    protected final List<Element<ManagedDocument>> drugAndAlcoholReportListRemoved;
    protected final List<Element<ManagedDocument>> lettersOfInstructionList;
    protected final List<Element<ManagedDocument>> lettersOfInstructionListLA;
    protected final List<Element<ManagedDocument>> lettersOfInstructionListCTSC;
    protected final List<Element<ManagedDocument>> lettersOfInstructionListRemoved;
    protected final List<Element<ManagedDocument>> adultPsychRepParentsList;
    protected final List<Element<ManagedDocument>> adultPsychRepParentsListLA;
    protected final List<Element<ManagedDocument>> adultPsychRepParentsListCTSC;
    protected final List<Element<ManagedDocument>> adultPsychRepParentsListRemoved;
    protected final List<Element<ManagedDocument>> famCentreAssessNonResList;
    protected final List<Element<ManagedDocument>> famCentreAssessNonResListLA;
    protected final List<Element<ManagedDocument>> famCentreAssessNonResListCTSC;
    protected final List<Element<ManagedDocument>> famCentreAssessNonResListRemoved;
    protected final List<Element<ManagedDocument>> familyCentreAssesResList;
    protected final List<Element<ManagedDocument>> familyCentreAssesResListLA;
    protected final List<Element<ManagedDocument>> familyCentreAssesResListCTSC;
    protected final List<Element<ManagedDocument>> familyCentreAssesResListRemoved;
    protected final List<Element<ManagedDocument>> haematologistList;
    protected final List<Element<ManagedDocument>> haematologistListLA;
    protected final List<Element<ManagedDocument>> haematologistListCTSC;
    protected final List<Element<ManagedDocument>> haematologistListRemoved;
    protected final List<Element<ManagedDocument>> indepSocialWorkerList;
    protected final List<Element<ManagedDocument>> indepSocialWorkerListLA;
    protected final List<Element<ManagedDocument>> indepSocialWorkerListCTSC;
    protected final List<Element<ManagedDocument>> indepSocialWorkerListRemoved;
    protected final List<Element<ManagedDocument>> multiDisciplinAssessList;
    protected final List<Element<ManagedDocument>> multiDisciplinAssessListLA;
    protected final List<Element<ManagedDocument>> multiDisciplinAssessListCTSC;
    protected final List<Element<ManagedDocument>> multiDisciplinAssessListRemoved;
    protected final List<Element<ManagedDocument>> neuroSurgeonList;
    protected final List<Element<ManagedDocument>> neuroSurgeonListLA;
    protected final List<Element<ManagedDocument>> neuroSurgeonListCTSC;
    protected final List<Element<ManagedDocument>> neuroSurgeonListRemoved;
    protected final List<Element<ManagedDocument>> ophthalmologistList;
    protected final List<Element<ManagedDocument>> ophthalmologistListLA;
    protected final List<Element<ManagedDocument>> ophthalmologistListCTSC;
    protected final List<Element<ManagedDocument>> ophthalmologistListRemoved;
    protected final List<Element<ManagedDocument>> otherExpertReportList;
    protected final List<Element<ManagedDocument>> otherExpertReportListLA;
    protected final List<Element<ManagedDocument>> otherExpertReportListCTSC;
    protected final List<Element<ManagedDocument>> otherExpertReportListRemoved;
    protected final List<Element<ManagedDocument>> otherMedicalReportList;
    protected final List<Element<ManagedDocument>> otherMedicalReportListLA;
    protected final List<Element<ManagedDocument>> otherMedicalReportListCTSC;
    protected final List<Element<ManagedDocument>> otherMedicalReportListRemoved;
    protected final List<Element<ManagedDocument>> pediatricList;
    protected final List<Element<ManagedDocument>> pediatricListLA;
    protected final List<Element<ManagedDocument>> pediatricListCTSC;
    protected final List<Element<ManagedDocument>> pediatricListRemoved;
    protected final List<Element<ManagedDocument>> pediatricRadiologistList;
    protected final List<Element<ManagedDocument>> pediatricRadiologistListLA;
    protected final List<Element<ManagedDocument>> pediatricRadiologistListCTSC;
    protected final List<Element<ManagedDocument>> pediatricRadiologistListRemoved;
    protected final List<Element<ManagedDocument>> profDNATestingList;
    protected final List<Element<ManagedDocument>> profDNATestingListLA;
    protected final List<Element<ManagedDocument>> profDNATestingListCTSC;
    protected final List<Element<ManagedDocument>> profDNATestingListRemoved;
    protected final List<Element<ManagedDocument>> profDrugAlcoholList;
    protected final List<Element<ManagedDocument>> profDrugAlcoholListLA;
    protected final List<Element<ManagedDocument>> profDrugAlcoholListCTSC;
    protected final List<Element<ManagedDocument>> profDrugAlcoholListRemoved;
    protected final List<Element<ManagedDocument>> professionalHairStrandList;
    protected final List<Element<ManagedDocument>> professionalHairStrandListLA;
    protected final List<Element<ManagedDocument>> professionalHairStrandListCTSC;
    protected final List<Element<ManagedDocument>> professionalHairStrandListRemoved;
    protected final List<Element<ManagedDocument>> professionalOtherList;
    protected final List<Element<ManagedDocument>> professionalOtherListLA;
    protected final List<Element<ManagedDocument>> professionalOtherListCTSC;
    protected final List<Element<ManagedDocument>> professionalOtherListRemoved;
    protected final List<Element<ManagedDocument>> psychiatricChildOnlyList;
    protected final List<Element<ManagedDocument>> psychiatricChildOnlyListLA;
    protected final List<Element<ManagedDocument>> psychiatricChildOnlyListCTSC;
    protected final List<Element<ManagedDocument>> psychiatricChildOnlyListRemoved;
    protected final List<Element<ManagedDocument>> psychChildParentCarersList;
    protected final List<Element<ManagedDocument>> psychChildParentCarersListLA;
    protected final List<Element<ManagedDocument>> psychChildParentCarersListCTSC;
    protected final List<Element<ManagedDocument>> psychChildParentCarersListRemoved;
    protected final List<Element<ManagedDocument>> psycReportChildClinList;
    protected final List<Element<ManagedDocument>> psycReportChildClinListLA;
    protected final List<Element<ManagedDocument>> psycReportChildClinListCTSC;
    protected final List<Element<ManagedDocument>> psycReportChildClinListRemoved;
    protected final List<Element<ManagedDocument>> psycReportChildOnlyEdList;
    protected final List<Element<ManagedDocument>> psycReportChildOnlyEdListLA;
    protected final List<Element<ManagedDocument>> psycReportChildOnlyEdListCTSC;
    protected final List<Element<ManagedDocument>> psycReportChildOnlyEdListRemoved;
    protected final List<Element<ManagedDocument>> psychReportParentChildList;
    protected final List<Element<ManagedDocument>> psychReportParentChildListLA;
    protected final List<Element<ManagedDocument>> psychReportParentChildListCTSC;
    protected final List<Element<ManagedDocument>> psychReportParentChildListRemoved;
    protected final List<Element<ManagedDocument>> psychRepParentFullCogList;
    protected final List<Element<ManagedDocument>> psychRepParentFullCogListLA;
    protected final List<Element<ManagedDocument>> psychRepParentFullCogListCTSC;
    protected final List<Element<ManagedDocument>> psychRepParentFullCogListRemoved;
    protected final List<Element<ManagedDocument>> psychRepParentFuncList;
    protected final List<Element<ManagedDocument>> psychRepParentFuncListLA;
    protected final List<Element<ManagedDocument>> psychRepParentFuncListCTSC;
    protected final List<Element<ManagedDocument>> psychRepParentFuncListRemoved;
    protected final List<Element<ManagedDocument>> toxicologyStatementList;
    protected final List<Element<ManagedDocument>> toxicologyStatementListLA;
    protected final List<Element<ManagedDocument>> toxicologyStatementListCTSC;
    protected final List<Element<ManagedDocument>> toxicologyStatementListRemoved;
    protected final List<Element<ManagedDocument>> expertReportList;
    protected final List<Element<ManagedDocument>> expertReportListLA;
    protected final List<Element<ManagedDocument>> expertReportListCTSC;
    protected final List<Element<ManagedDocument>> expertReportListRemoved;
    protected final List<Element<ManagedDocument>> policeDisclosureList;
    protected final List<Element<ManagedDocument>> policeDisclosureListLA;
    protected final List<Element<ManagedDocument>> policeDisclosureListCTSC;
    protected final List<Element<ManagedDocument>> policeDisclosureListRemoved;
    protected final List<Element<ManagedDocument>> medicalRecordList;
    protected final List<Element<ManagedDocument>> medicalRecordListLA;
    protected final List<Element<ManagedDocument>> medicalRecordListCTSC;
    protected final List<Element<ManagedDocument>> medicalRecordListRemoved;
    protected final List<Element<ManagedDocument>> noticeOfActingOrIssueList;
    protected final List<Element<ManagedDocument>> noticeOfActingOrIssueListLA;
    protected final List<Element<ManagedDocument>> noticeOfActingOrIssueListCTSC;
    protected final List<Element<ManagedDocument>> noticeOfActingOrIssueListRemoved;

    protected final List<Element<ManagedDocument>> parentAssessmentList;
    protected final List<Element<ManagedDocument>> parentAssessmentListLA;
    protected final List<Element<ManagedDocument>> parentAssessmentListCTSC;
    protected final List<Element<ManagedDocument>> parentAssessmentListRemoved;
    protected final List<Element<ManagedDocument>> famAndViabilityList;
    protected final List<Element<ManagedDocument>> famAndViabilityListLA;
    protected final List<Element<ManagedDocument>> famAndViabilityListCTSC;
    protected final List<Element<ManagedDocument>> famAndViabilityListRemoved;
    protected final List<Element<ManagedDocument>> applicantOtherDocList;
    protected final List<Element<ManagedDocument>> applicantOtherDocListLA;
    protected final List<Element<ManagedDocument>> applicantOtherDocListCTSC;
    protected final List<Element<ManagedDocument>> applicantOtherDocListRemoved;
    protected final List<Element<ManagedDocument>> meetingNoteList;
    protected final List<Element<ManagedDocument>> meetingNoteListLA;
    protected final List<Element<ManagedDocument>> meetingNoteListCTSC;
    protected final List<Element<ManagedDocument>> meetingNoteListRemoved;
    protected final List<Element<ManagedDocument>> contactNoteList;
    protected final List<Element<ManagedDocument>> contactNoteListLA;
    protected final List<Element<ManagedDocument>> contactNoteListCTSC;
    protected final List<Element<ManagedDocument>> contactNoteListRemoved;
    protected final List<Element<ManagedDocument>> judgementList;
    protected final List<Element<ManagedDocument>> judgementListLA;
    protected final List<Element<ManagedDocument>> judgementListCTSC;
    protected final List<Element<ManagedDocument>> judgementListRemoved;
    protected final List<Element<ManagedDocument>> transcriptList;
    protected final List<Element<ManagedDocument>> transcriptListLA;
    protected final List<Element<ManagedDocument>> transcriptListCTSC;
    protected final List<Element<ManagedDocument>> transcriptListRemoved;
    protected final List<Element<ManagedDocument>> respWitnessStmtList;
    protected final List<Element<ManagedDocument>> respWitnessStmtListLA;
    protected final List<Element<ManagedDocument>> respWitnessStmtListCTSC;
    protected final List<Element<ManagedDocument>> respWitnessStmtListRemoved;
    protected final List<Element<ManagedDocument>> previousProceedingList;
    protected final List<Element<ManagedDocument>> previousProceedingListLA;
    protected final List<Element<ManagedDocument>> previousProceedingListCTSC;
    protected final List<Element<ManagedDocument>> previousProceedingListRemoved;
    protected final List<Element<ManagedDocument>> thresholdList;
    protected final List<Element<ManagedDocument>> thresholdListLA;
    protected final List<Element<ManagedDocument>> thresholdListCTSC;
    protected final List<Element<ManagedDocument>> thresholdListRemoved;
    protected final List<Element<ManagedDocument>> documentsFiledOnIssueList;
    protected final List<Element<ManagedDocument>> documentsFiledOnIssueListLA;
    protected final List<Element<ManagedDocument>> documentsFiledOnIssueListCTSC;
    protected final List<Element<ManagedDocument>> documentsFiledOnIssueListRemoved;
    protected final List<Element<ManagedDocument>> carePlanList;
    protected final List<Element<ManagedDocument>> carePlanListLA;
    protected final List<Element<ManagedDocument>> carePlanListCTSC;
    protected final List<Element<ManagedDocument>> carePlanListRemoved;

    protected final List<Element<ManagedDocument>> correspondenceDocList;
    protected final List<Element<ManagedDocument>> correspondenceDocListLA;
    protected final List<Element<ManagedDocument>> correspondenceDocListCTSC;
    protected final List<Element<ManagedDocument>> correspondenceDocListRemoved;

    protected final List<Element<ManagedDocument>> archivedDocumentsList;
    protected final List<Element<ManagedDocument>> archivedDocumentsListLA;
    protected final List<Element<ManagedDocument>> archivedDocumentsListCTSC;
    protected final List<Element<ManagedDocument>> archivedDocumentsListRemoved;


    protected final List<Element<ManagedDocument>> c1ApplicationDocList;
    protected final List<Element<ManagedDocument>> c1ApplicationDocListLA;
    protected final List<Element<ManagedDocument>> c1ApplicationDocListCTSC;
    protected final List<Element<ManagedDocument>> c1ApplicationDocListRemoved;
    protected final List<Element<ManagedDocument>> c2ApplicationDocList;
    protected final List<Element<ManagedDocument>> c2ApplicationDocListLA;
    protected final List<Element<ManagedDocument>> c2ApplicationDocListCTSC;
    protected final List<Element<ManagedDocument>> c2ApplicationDocListRemoved;

    @NotNull(message = "Add the grounds for the application", groups = SecureAccommodationGroup.class)
    @Valid
    protected final GroundsForSecureAccommodationOrder groundsForSecureAccommodationOrder;

    @NotNull(message = "Add the grounds for the application")
    @Valid
    protected final GroundsForRefuseContactWithChild groundsForRefuseContactWithChild;

    @NotNull(message = "Add the grounds for the application")
    @Valid
    protected final GroundsForChildRecoveryOrder groundsForChildRecoveryOrder;

    @NotNull(message = "Add the grounds for the application")
    @Valid
    protected final GroundsForContactWithChild groundsForContactWithChild;

    protected final YesNo skipPaymentPage;

    @NotNull(message = "Add the grounds for the application")
    @Valid
    protected final GroundsForEducationSupervisionOrder groundsForEducationSupervisionOrder;

    protected final YesNo shouldSendOrderReminder;

    protected final CaseLocation caseManagementLocation;
    protected final List<Element<ListingActionRequest>> listingRequests;
    protected final ListingActionRequest listingRequestToReview;
    protected final List<Element<ListingActionRequest>> reviewedListingRequests;
    protected final List<ListingActionType> selectListingActions;
    protected final String listingDetails;
    protected final DynamicList listingRequestsList;

    public List<Element<ManagedDocument>> getGuardianReportsList() {
        return guardianReportsList != null ? guardianReportsList : new ArrayList<>();
    }
}
