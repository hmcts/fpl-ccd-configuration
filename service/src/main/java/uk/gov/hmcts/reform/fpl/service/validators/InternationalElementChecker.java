package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.EventCheckerHelper.anyNonEmpty;

@Component
public class InternationalElementChecker implements EventChecker {

    @Override
    public List<String> validate(CaseData caseData) {
        return emptyList();
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        final InternationalElement internationalElement = caseData.getInternationalElement();

        if (isEmpty(internationalElement)) {
            return false;
        }

        return anyNonEmpty(
            internationalElement.getIssues(),
            internationalElement.getProceedings(),
            internationalElement.getPossibleCarer(),
            internationalElement.getSignificantEvents(),
            internationalElement.getInternationalAuthorityInvolvement());
    }

    @Override
    public boolean isCompleted(CaseData caseData) {
        final InternationalElement internationalElement = caseData.getInternationalElement();

        if (internationalElement == null || anyEmpty(
            internationalElement.getIssues(),
            internationalElement.getProceedings(),
            internationalElement.getPossibleCarer(),
            internationalElement.getSignificantEvents(),
            internationalElement.getInternationalAuthorityInvolvement())) {
            return false;
        }

        if (internationalElement.getIssues().equals("Yes")
            && isNullOrEmpty(internationalElement.getIssuesReason())) {
            return false;
        } else if (internationalElement.getProceedings().equals("Yes")
            && isNullOrEmpty(internationalElement.getProceedingsReason())) {
            return false;
        } else if (internationalElement.getPossibleCarer().equals("Yes")
            && isNullOrEmpty(internationalElement.getPossibleCarerReason())) {
            return false;
        } else if (internationalElement.getSignificantEvents().equals("Yes")
            && isNullOrEmpty(internationalElement.getSignificantEventsReason())) {
            return false;
        } else {
            return !internationalElement.getInternationalAuthorityInvolvement().equals("Yes")
                ||
                !isNullOrEmpty(internationalElement.getInternationalAuthorityInvolvementDetails());
        }
    }
}
