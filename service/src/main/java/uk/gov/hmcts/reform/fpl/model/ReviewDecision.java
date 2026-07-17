package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.CMOReviewOutcome;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class ReviewDecision {
    @CCD(label = " ", showCondition = "decision=\"DO_NOT_SHOW\"", typeOverride = FieldType.Document)
    private final DocumentReference document;
    @CCD(
            label = "Use the link above to download the order. After you’ve made changes, save the final order in Word and upload. This will then be sealed and sent to all parties.",
            showCondition = "decision=\"JUDGE_AMENDS_DRAFT\"",
            regex = ".doc,.docx,.pdf",
            typeOverride = FieldType.Document
    )
    private final DocumentReference judgeAmendedDocument;
    @CCD(label = " ", showCondition = "decision=\"DO_NOT_SHOW\"")
    private final String hearing;
    @CCD(label = "Is this order ready to be sealed and issued?")
    private final CMOReviewOutcome decision;
    @CCD(
            label = "What do they need to change?",
            showCondition = "decision=\"JUDGE_REQUESTED_CHANGES\"",
            typeOverride = FieldType.TextArea
    )
    private final String changesRequestedByJudge;

    @JsonIgnore
    public boolean hasReviewOutcomeOf(CMOReviewOutcome reviewOutcome) {
        return reviewOutcome.equals(decision);
    }

    @JsonIgnore
    public boolean hasBeenApproved() {
        return CMOReviewOutcome.SEND_TO_ALL_PARTIES.equals(decision)
            || CMOReviewOutcome.JUDGE_AMENDS_DRAFT.equals(decision);
    }
}
