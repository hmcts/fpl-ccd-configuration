package uk.gov.hmcts.reform.fpl.controllers.guards;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.FplEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CompoundEventValidator implements EventValidator {

    @Autowired
    EventValidatorProvider eventValidatorProvider;

    public List<String> validate(CaseData caseData, List<FplEvent> events) {
        Map<FplEvent, List<String>> errors = new HashMap<>();

        events.forEach(e -> errors.put(e, eventValidatorProvider.validate(e, caseData)));


        List<String> ee = new ArrayList<>();

        errors.forEach((k, v) -> {
            if (ObjectUtils.isNotEmpty(v)) {
                ee.add(String.format("In the %s section:", k.getName()));
                v.forEach(x -> ee.add(String.format("• %s", x)));
            }
        });

        return ee;

    }
}
