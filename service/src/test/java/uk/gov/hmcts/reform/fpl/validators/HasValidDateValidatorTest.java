package uk.gov.hmcts.reform.fpl.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.HearingBookingDetail;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)

public class HasValidDateValidatorTest {
    private Validator validator;

    private static final String ERROR_MESSAGE = "Enter a future date";

    @BeforeEach
    private void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldNotReturnAnErrorIfDateInTheFuture() {
        HearingBookingDetail hearingBookingDetail = HearingBookingDetail.builder()
            .hearingDate(LocalDate.now())
            .build();

        List<String> errorMessages = validator.validate(hearingBookingDetail).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }
}
