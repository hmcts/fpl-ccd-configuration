package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.validation.groups.SecureAccommodationGroup;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@JsonSubTypes({
    @JsonSubTypes.Type(value = CaseData.class)
})
@Jacksonized
@SuperBuilder(toBuilder = true)
@Data
public class CaseDataParent {

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
    protected final List<Element<ManagedDocument>> expertReportList;
    protected final List<Element<ManagedDocument>> expertReportListLA;
    protected final List<Element<ManagedDocument>> expertReportListCTSC;
    protected final List<Element<ManagedDocument>> expertReportListRemoved;
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
}
