package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;

import java.util.List;

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

        if (("Yes").equals(internationalElement.getIssues())
            && isEmpty(internationalElement.getIssuesReason())) {
            return false;
        } else if (("Yes").equals(internationalElement.getProceedings())
            && isEmpty(internationalElement.getProceedingsReason())) {
            return false;
        } else if (("Yes").equals(internationalElement.getPossibleCarer())
            && isEmpty(internationalElement.getPossibleCarerReason())) {
            return false;
        } else if (("Yes").equals(internationalElement.getSignificantEvents())
            && isEmpty(internationalElement.getSignificantEventsReason())) {
            return false;
        } else {
            return ("No").equals(internationalElement.getInternationalAuthorityInvolvement())
                || !isEmpty(internationalElement.getInternationalAuthorityInvolvementDetails());
        }
    }
}
