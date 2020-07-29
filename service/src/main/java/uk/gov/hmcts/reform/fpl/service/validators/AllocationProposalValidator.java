package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

@Component
public class AllocationProposalValidator extends PropertiesValidator {

    @Override
    public List<String> validate(CaseData caseData) {
        return super.validate(caseData, List.of("allocationProposal"));
    }
}
