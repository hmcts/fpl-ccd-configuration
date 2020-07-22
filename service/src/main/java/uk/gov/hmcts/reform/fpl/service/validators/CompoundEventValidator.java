package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.FplEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public abstract class CompoundEventValidator implements Validator {

    @Autowired
    private EventChecker eventValidatorProvider;

    public List<String> validate(CaseData caseData, List<FplEvent> events) {

        final Map<FplEvent, List<String>> errorsByEvent = events.stream()
            .collect(toMap(identity(), event -> eventValidatorProvider.validate(event, caseData)));

        List<String> groupedErrors = new ArrayList<>();

        errorsByEvent.forEach((event, errors) -> {
            if (isNotEmpty(errors)) {
                groupedErrors.add(String.format("In the %s section:", event.getName().toLowerCase()));
                errors.forEach(error -> groupedErrors.add(String.format("• %s", error)));
            }
        });

        return groupedErrors;
    }
}
