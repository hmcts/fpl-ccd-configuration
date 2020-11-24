package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public abstract class CompoundEventChecker implements EventChecker {

    @Autowired
    private EventsChecker eventChecker;

    @Autowired
    private FeatureToggleService featureToggleService;

    public List<String> validate(CaseData caseData, List<Event> events) {
        return events.stream()
                .flatMap(event -> {
                    if(featureToggleService.isApplicationDocumentsEventEnabled() && event.equals(Event.DOCUMENTS)) {
                        return Stream.empty();
                    }
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
