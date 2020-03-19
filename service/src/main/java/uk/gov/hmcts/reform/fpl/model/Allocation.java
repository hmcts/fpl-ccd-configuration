package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import uk.gov.hmcts.ccd.sdk.types.ComplexType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@ComplexType(name = "AllocationDecision")
public class Allocation {
    @NotBlank(message = "Enter an allocation proposal")
    private final String proposal;
    private final String proposalReason;
    private final String allocationProposalPresent;
    private final String judgeLevelRadio;
}
