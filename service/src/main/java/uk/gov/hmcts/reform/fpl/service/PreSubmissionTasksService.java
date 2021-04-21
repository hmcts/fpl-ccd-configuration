package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidation;
import uk.gov.hmcts.reform.fpl.service.validators.EventsChecker;

import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker.getRequiredEvents;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PreSubmissionTasksService {
    private final EventsChecker eventChecker;

    public List<EventValidation> getEventValidationsForSubmission(CaseData caseData) {
        List<Event> events = getRequiredEvents();
        List<EventValidation> validations = new LinkedList<>();
        events.forEach(
            event -> {
                List<String> errors = eventChecker.validate(event, caseData);
                if (isNotEmpty(errors)) {
                    validations.add(EventValidation.builder().event(event).messages(errors).build());
                }
            }
        );
        return validations;
    }
}
