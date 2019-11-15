package uk.gov.hmcts.reform.fpl.validation.validators;

import org.junit.jupiter.api.BeforeAll;

import javax.validation.Validation;
import javax.validation.Validator;

public abstract class ValidatorTest {
    protected static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
