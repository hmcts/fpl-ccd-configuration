package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;

@Component
public class AllocationProposalValidator extends PropertiesValidator {

    public AllocationProposalValidator() {
        super("allocationProposal");
    }
}
