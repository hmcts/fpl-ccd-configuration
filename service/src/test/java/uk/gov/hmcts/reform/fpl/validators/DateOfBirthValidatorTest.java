package uk.gov.hmcts.reform.fpl.validators;

import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.validators.DateOfBirthValidator.dateOfBirthIsInFuture;

public class DateOfBirthValidatorTest {

    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    @Test
    void shouldReturnTrueIfDobIsTomorrow() {
        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.DATE, 1);
        dt = c.getTime();
        assertThat(dateOfBirthIsInFuture(df.format(dt))).isTrue();
    }

    @Test
    void shouldReturnFalseIfDobIsInPast() {
        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.DATE, -1);
        dt = c.getTime();
        assertThat(dateOfBirthIsInFuture(df.format(dt))).isFalse();
    }

    @Test
    void shouldReturnFalseIfDobIsInToday() {
        Date dt = new Date();
        assertThat(dateOfBirthIsInFuture(df.format(dt))).isFalse();
    }

}
