package uk.gov.hmcts.reform.fpl.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.AllocationProposaJudgeTypes.CIRCUIT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.AllocationProposaJudgeTypes.CIRCUIT_JUDGE_SECTION_9;


@Data
@Jacksonized
@Builder(toBuilder = true)
public class Allocation {
    @Deprecated
    @CCD(
            label = "Which level of judge do you recommend for this case?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "AllocationProposalList"
    )
    private final String proposal;
    @NotBlank(message = "Enter an allocation proposal")
    @CCD(
            label = "Which level of judge do you recommend for this case?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "AllocationProposalListV2"
    )
    private final String proposalV2;
    @NotBlank(message = "Enter an allocation proposal reason")
    @CCD(label = "Reasons for recommendation", typeOverride = FieldType.TextArea)
    private final String proposalReason;
    private final String allocationProposalPresent;
    private final String judgeLevelRadio;

    // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
    @CCD(label = " ", typeOverride = FieldType.Label)
    private String reasonLabel1;
    @CCD(label = "The proposed allocation is ${allocationProposal.proposalV2}.", typeOverride = FieldType.Label)
    private String allocationDecisionLabel;
    @CCD(label = "${allocationProposal.proposalReason}", typeOverride = FieldType.Label)
    private String allocationDecisionLabelReason;
    @CCD(label = "The local authority has not made an allocation proposal.", typeOverride = FieldType.Label)
    private String missingAllocationDecisionLabel;
    // ==== end synthesised definition-only fields ====


    public static class AllocationBuilder {

        @Deprecated
        public Allocation.AllocationBuilder proposal(String proposal) {
            this.proposal = proposal;
            if (isEmpty(this.proposalV2)) {
                if (CIRCUIT_JUDGE_SECTION_9.getValue().equals(proposal)) {
                    this.proposalV2 = CIRCUIT_JUDGE.getValue();
                } else {
                    this.proposalV2 = proposal;
                }
            }
            return this;
        }

        public Allocation.AllocationBuilder proposalV2(String proposalV2) {
            if (!isEmpty(proposalV2)) {
                this.proposalV2 = proposalV2;
            }
            return this;
        }
    }
}
