package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskState;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskState.COMPLETED_FINISHED;
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

        if (YES.getValue().equals(internationalElement.getIssues())
            && isEmpty(internationalElement.getIssuesReason())) {
            return false;
        }

        if (YES.getValue().equals(internationalElement.getProceedings())
            && isEmpty(internationalElement.getProceedingsReason())) {
            return false;
        }

        if (YES.getValue().equals(internationalElement.getPossibleCarer())
            && isEmpty(internationalElement.getPossibleCarerReason())) {
            return false;
        }

        if (YES.getValue().equals(internationalElement.getSignificantEvents())
            && isEmpty(internationalElement.getSignificantEventsReason())) {
            return false;
        }

        return NO.getValue().equals(internationalElement.getInternationalAuthorityInvolvement())
            || !isEmpty(internationalElement.getInternationalAuthorityInvolvementDetails());
    }

    @Override
    public TaskState completedState() {
        return COMPLETED_FINISHED;
    }
}
