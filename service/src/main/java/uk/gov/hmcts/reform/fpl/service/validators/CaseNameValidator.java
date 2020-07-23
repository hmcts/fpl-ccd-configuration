package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;

@Component
public class CaseNameValidator extends PropertiesValidator {

    public CaseNameValidator() {
        super("caseName");
    }
}
