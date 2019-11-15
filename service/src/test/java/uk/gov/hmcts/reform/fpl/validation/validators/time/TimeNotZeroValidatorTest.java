package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class TimeNotZeroValidatorTest extends TimeValidatorTest {



    @Test
    void shouldReturnAnErrorWhenAllTimeFieldsAreZero() {

    }
}
