package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;

@Component
public class GroundsValidator extends PropertiesValidator {

    public GroundsValidator() {
        super("grounds");
    }
}
