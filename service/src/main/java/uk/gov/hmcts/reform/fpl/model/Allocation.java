package uk.gov.hmcts.reform.fpl.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.AllocationProposaJudgeTypes.CIRCUIT_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.AllocationProposaJudgeTypes.CIRCUIT_JUDGE_SECTION_9;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;


@Data
@Jacksonized
@Builder(toBuilder = true)
public class Allocation {
    @Deprecated
    private final String proposal;
    @NotBlank(message = "Enter an allocation proposal")
    private final String proposalV2;
    @NotBlank(message = "Enter an allocation proposal reason")
    private final String proposalReason;
    private final String allocationProposalPresent;
    private final String judgeLevelRadio;


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
