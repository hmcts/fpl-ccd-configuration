package uk.gov.hmcts.reform.fpl.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Allocation {
    @NotBlank(message = "Enter an allocation proposal")
    private final String proposal;
    @NotBlank(message = "Enter an allocation proposal reason")
    private final String proposalReason;
    private final String allocationProposalPresent;
    private final String judgeLevelRadio;
}
