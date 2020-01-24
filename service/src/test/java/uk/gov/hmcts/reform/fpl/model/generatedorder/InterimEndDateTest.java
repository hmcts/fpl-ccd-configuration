package uk.gov.hmcts.reform.fpl.model.generatedorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = FixedTimeConfiguration.class)
class InterimEndDateTest {
    @Autowired
    private static Time time;

    private InterimEndDate interimEndDate;

    @Test
    void shouldReturnDateTimeWithSameDateAndTimeAtEndOfDay() {
        interimEndDate = buildInterimEndDate(null, time.now().toLocalDate());
        final LocalDateTime expected = LocalDateTime.of(time.now().toLocalDate(), LocalTime.of(23,59,59));
        assertThat(interimEndDate.toLocalDateTime()).isEqualToIgnoringNanos(expected);
    }

    @Test
    void shouldReturnNullWhenEndDateIsNull() {
        interimEndDate = buildInterimEndDate(null, null);
        assertThat(interimEndDate.toLocalDateTime()).isNull();
    }

    @Test
    void shouldReturnTrueWhenTypeIsNamedAndEndDateIsNotNull() {
        interimEndDate = buildInterimEndDate(NAMED_DATE, time.now().toLocalDate());
        assertThat(interimEndDate.hasEndDate()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("hasEndDateSource")
    void shouldReturnFalseWhenTypeIsNotNamedOrEndDateIsNull(InterimEndDateType type, LocalDate localDate) {
        interimEndDate = buildInterimEndDate(type, localDate);
        assertThat(interimEndDate.hasEndDate()).isFalse();
    }

    private static Stream<Arguments> hasEndDateSource() {
        return Stream.of(
            Arguments.of(NAMED_DATE, null),
            Arguments.of(END_OF_PROCEEDINGS, time.now()),
            Arguments.of(END_OF_PROCEEDINGS, null),
            Arguments.of(null, time.now()),
            Arguments.of(null, null)
        );
    }

    private InterimEndDate buildInterimEndDate(InterimEndDateType type, LocalDate localDate) {
        return InterimEndDate.builder()
            .endDateType(type)
            .endDate(localDate)
            .build();
    }
}
