package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
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

    protected final List<Element<DocumentReference>> parentAssessmentList;
    protected final List<Element<DocumentReference>> parentAssessmentListLA;
    protected final List<Element<DocumentReference>> parentAssessmentListCTSC;
    protected final List<Element<DocumentReference>> famAndViabilityList;
    protected final List<Element<DocumentReference>> famAndViabilityListLA;
    protected final List<Element<DocumentReference>> famAndViabilityListCTSC;
    protected final List<Element<DocumentReference>> applicantOtherDocList;
    protected final List<Element<DocumentReference>> applicantOtherDocListLA;
    protected final List<Element<DocumentReference>> applicantOtherDocListCTSC;
    protected final List<Element<DocumentReference>> meetingNoteList;
    protected final List<Element<DocumentReference>> meetingNoteListLA;
    protected final List<Element<DocumentReference>> meetingNoteListCTSC;
    protected final List<Element<DocumentReference>> contactNoteList;
    protected final List<Element<DocumentReference>> contactNoteListLA;
    protected final List<Element<DocumentReference>> contactNoteListCTSC;
    protected final List<Element<DocumentReference>> judgementList;
    protected final List<Element<DocumentReference>> judgementListLA;
    protected final List<Element<DocumentReference>> judgementListCTSC;
    protected final List<Element<DocumentReference>> transcriptList;
    protected final List<Element<DocumentReference>> transcriptListLA;
    protected final List<Element<DocumentReference>> transcriptListCTSC;
    protected final List<Element<DocumentReference>> respWitnessStmtList;
    protected final List<Element<DocumentReference>> respWitnessStmtListLA;
    protected final List<Element<DocumentReference>> respWitnessStmtListCTSC;

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
