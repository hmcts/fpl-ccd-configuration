package uk.gov.hmcts.reform.fpl.model.order.generated;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.InterimEndDateType.NAMED_DATE;

class InterimEndDateTest {

    private InterimEndDate interimEndDate;
    private static final LocalDate now = LocalDate.now();

    @Test
    void shouldReturnDateTimeWithSameDateAndTimeAtEndOfDay() {
        interimEndDate = buildInterimEndDate(NAMED_DATE, now);
        final LocalDateTime expected = LocalDateTime.of(now, LocalTime.of(23,59,59));
        final Optional<LocalDateTime> actual = interimEndDate.toLocalDateTime();
        assertThat(actual).contains(expected);
    }

    @Test
    void shouldReturnNullWhenEndDateIsNull() {
        interimEndDate = buildInterimEndDate(null, null);
        final Optional<LocalDateTime> actual = interimEndDate.toLocalDateTime();
        assertThat(actual).isEmpty();
    }

    private InterimEndDate buildInterimEndDate(InterimEndDateType type, LocalDate localDate) {
        return InterimEndDate.builder()
            .type(type)
            .endDate(localDate)
            .build();
    }
}
