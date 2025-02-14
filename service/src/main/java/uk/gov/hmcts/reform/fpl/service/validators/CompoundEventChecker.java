package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidationErrors;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public abstract class CompoundEventChecker implements EventChecker {

    @Autowired
    private EventsChecker eventChecker;

    public List<String> validate(CaseData caseData, List<Event> events) {

        List<EventValidationErrors> eventsErrors = validateEvents(caseData, events);

        return eventsErrors.stream()
            .flatMap(eventErrors -> {
                String groupName = format("In the %s section:", eventErrors.getEvent().getName().toLowerCase());

                // get the English errors (for now, until we can check user language inside events)
                List<String> formattedEventErrors = eventErrors.getErrors().stream()
                    .map(error -> format("• %s", Arrays.stream(error.split("\\|")).findFirst().orElse("")))
                    .filter(s -> !s.isBlank())
                    .collect(toList());

                formattedEventErrors.add(0, groupName);

                return formattedEventErrors.stream();
            })
            .collect(toList());
    }

    public List<EventValidationErrors> validateEvents(CaseData caseData, List<Event> events) {
        return events.stream()
            .map(event -> EventValidationErrors.builder()
                .event(event)
                .errors(eventChecker.validate(event, caseData).stream()
                    .distinct()
                    .sorted()
                    .collect(toList()))
                .build())
            .filter(eventErrors -> isNotEmpty(eventErrors.getErrors()))
            .collect(toList());
    }
}
