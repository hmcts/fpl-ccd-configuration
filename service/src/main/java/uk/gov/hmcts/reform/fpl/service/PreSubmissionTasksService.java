package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.submission.PreSubmissionTask;
import uk.gov.hmcts.reform.fpl.service.validators.EventsChecker;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker.getRequiredEvents;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PreSubmissionTasksService {
    private final EventsChecker eventChecker;

    public List<PreSubmissionTask> getPreSubmissionTasks(CaseData caseData) {
        List<Event> events = getRequiredEvents();

        return events.stream()
            .map(event ->
                PreSubmissionTask.builder()
                    .event(event)
                    .messages(eventChecker.validate(event, caseData))
                    .build())
            .collect(Collectors.toList());
    }
}
