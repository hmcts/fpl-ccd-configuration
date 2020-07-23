package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;

@Component
public class ChildrenValidator extends PropertiesValidator {

    public ChildrenValidator() {
        super("children1");
    }
}
