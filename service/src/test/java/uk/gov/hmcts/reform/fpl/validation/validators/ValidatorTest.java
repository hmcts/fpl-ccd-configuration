package uk.gov.hmcts.reform.fpl.validation.validators;

import javax.validation.Validator;

public abstract class ValidatorTest {
    protected final Validator validator;

    public ValidatorTest(Validator validator) {
        this.validator = validator;
    }
}
