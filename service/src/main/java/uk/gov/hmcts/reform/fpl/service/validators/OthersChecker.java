package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

import static java.util.Collections.emptyList;

@Component
public class OthersChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return emptyList();
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        return false;
    }
}
