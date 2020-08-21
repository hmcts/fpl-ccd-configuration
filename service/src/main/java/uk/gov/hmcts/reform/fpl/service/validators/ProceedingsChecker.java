package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Proceeding;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
public class ProceedingsChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return emptyList();
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        Proceeding proceedings = caseData.getProceeding();
        return isNotEmpty(proceedings) && isNotEmpty(proceedings.getOnGoingProceeding());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        return false;
    }
}
