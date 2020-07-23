package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;

@Component
public class RespondentsValidator extends PropertiesValidator {

    public RespondentsValidator() {
        super("respondents1");
    }
}
