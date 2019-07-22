package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class DateUtilsTest {

    @Test
    void givenValidDateShouldReturnDateAsAString() {
        LocalDateTime now = LocalDateTime.now();
        String actual = DateUtils.convertLocalDateTimeToString(now);
        assertThat(actual).isNotNull();
        assertThat(actual.length()).isEqualTo(10);
    }

    @Test
    void givenNullDateShouldReturnNull() {
        String actual = DateUtils.convertLocalDateTimeToString(null);
        assertThat(actual).isNull();
    }
}
