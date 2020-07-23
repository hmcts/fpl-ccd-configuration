package uk.gov.hmcts.reform.fpl.service.validators;

import org.springframework.stereotype.Component;

@Component
public class ApplicantValidator extends PropertiesValidator {

    public ApplicantValidator() {
        super("applicants", "solicitor");
    }
}
