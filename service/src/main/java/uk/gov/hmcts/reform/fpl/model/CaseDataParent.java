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

    protected final List<Element<SupportingEvidenceBundle>> parentAssessmentList;
    protected final List<Element<SupportingEvidenceBundle>> parentAssessmentListLA;
    protected final List<Element<SupportingEvidenceBundle>> parentAssessmentListCTSC;
    protected final List<Element<SupportingEvidenceBundle>> parentAssessmentListRemoved;
    protected final List<Element<SupportingEvidenceBundle>> famAndViabilityList;
    protected final List<Element<SupportingEvidenceBundle>> famAndViabilityListLA;
    protected final List<Element<SupportingEvidenceBundle>> famAndViabilityListCTSC;
    protected final List<Element<SupportingEvidenceBundle>> famAndViabilityListRemoved;
    protected final List<Element<SupportingEvidenceBundle>> applicantOtherDocList;
    protected final List<Element<SupportingEvidenceBundle>> applicantOtherDocListLA;
    protected final List<Element<SupportingEvidenceBundle>> applicantOtherDocListCTSC;
    protected final List<Element<SupportingEvidenceBundle>> applicantOtherDocListRemoved;
    protected final List<Element<SupportingEvidenceBundle>> meetingNoteList;
    protected final List<Element<SupportingEvidenceBundle>> meetingNoteListLA;
    protected final List<Element<SupportingEvidenceBundle>> meetingNoteListCTSC;
    protected final List<Element<SupportingEvidenceBundle>> meetingNoteListRemoved;
    protected final List<Element<SupportingEvidenceBundle>> contactNoteList;
    protected final List<Element<SupportingEvidenceBundle>> contactNoteListLA;
    protected final List<Element<SupportingEvidenceBundle>> contactNoteListCTSC;
    protected final List<Element<SupportingEvidenceBundle>> contactNoteListRemoved;
    protected final List<Element<SupportingEvidenceBundle>> judgementList;
    protected final List<Element<SupportingEvidenceBundle>> judgementListLA;
    protected final List<Element<SupportingEvidenceBundle>> judgementListCTSC;
    protected final List<Element<SupportingEvidenceBundle>> judgementListRemoved;
    protected final List<Element<SupportingEvidenceBundle>> transcriptList;
    protected final List<Element<SupportingEvidenceBundle>> transcriptListLA;
    protected final List<Element<SupportingEvidenceBundle>> transcriptListCTSC;
    protected final List<Element<SupportingEvidenceBundle>> transcriptListRemoved;
    protected final List<Element<SupportingEvidenceBundle>> respWitnessStmtList;
    protected final List<Element<SupportingEvidenceBundle>> respWitnessStmtListLA;
    protected final List<Element<SupportingEvidenceBundle>> respWitnessStmtListCTSC;
    protected final List<Element<SupportingEvidenceBundle>> respWitnessStmtListRemoved;

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
