package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;

@Component
public class HearingNeededValidator extends PropertiesValidator {

    public HearingNeededValidator() {
        super("hearing");
    }
}
