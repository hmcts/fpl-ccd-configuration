package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public abstract class CompoundEventChecker implements EventChecker {

    @Autowired
    private EventsChecker eventChecker;

    public List<String> validate(CaseData caseData, List<Event> events) {
        return events.stream()
                .flatMap(event -> {

                    List<String> groupErrors = new ArrayList<>();
                    List<String> errors = eventChecker.validate(event, caseData);

                    if (isNotEmpty(errors)) {
                        groupErrors.add(String.format("In the %s section:", event.getName().toLowerCase()));
                        errors.stream().distinct().forEach(error -> groupErrors.add(String.format("• %s", error)));
                    }
                    return groupErrors.stream();
                })
                .collect(Collectors.toList());
    }
}
