package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor

//TODO: This annotation should stop java from pushing values with null values to ccd (which would error)
// Just double check
@JsonInclude(JsonInclude.Include.NON_NULL)

//TODO: rename class to reflect being used for proposal and decision. CCD can be different objects, here we can be
// clever and have one object OR I guess you could extend and have a new class with just the new fields. Up to you.
public class AllocationDecision {
    /*
        The below are called proposal as ideally we would like to reuse the allocationProposal complex type,
        but had some issues with labels so there is another complex type. The field could be renamed as tech debt.
     */
    private final String proposal;
    private final String proposalReason;
    private final String allocationProposalPresent;
    private final String judgeLevelRadio;
}
