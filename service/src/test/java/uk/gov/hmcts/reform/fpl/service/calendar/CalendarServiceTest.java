package uk.gov.hmcts.reform.fpl.service.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.util.Set;

import static java.time.Month.APRIL;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.MAY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FixedTimeConfiguration.class})
class CalendarServiceTest {

    private static final LocalDate SATURDAY = LocalDate.of(2020, APRIL, 4);
    private static final LocalDate SUNDAY = LocalDate.of(2020, APRIL, 5);

    private static final LocalDate NEW_YEAR = LocalDate.of(2020, JANUARY, 1);
    private static final LocalDate GOOD_FRIDAY = LocalDate.of(2020, APRIL, 10);
    private static final LocalDate EASTER_MONDAY = LocalDate.of(2020, APRIL, 13);
    private static final LocalDate EARLY_MAY_BANK_HOLIDAY = LocalDate.of(2020, MAY, 8);
    private static final LocalDate SPRING_BANK_HOLIDAY = LocalDate.of(2020, MAY, 25);
    private static final LocalDate SUMMER_BANK_HOLIDAY = LocalDate.of(2020, AUGUST, 31);
    private static final LocalDate CHRISTMAS_DAY = LocalDate.of(2020, DECEMBER, 25);
    private static final LocalDate BOXING_DAY = LocalDate.of(2020, DECEMBER, 28);

    private static final Set<LocalDate> BANK_HOLIDAYS = Set.of(
        NEW_YEAR,
        GOOD_FRIDAY,
        EASTER_MONDAY,
        EARLY_MAY_BANK_HOLIDAY,
        SPRING_BANK_HOLIDAY,
        SUMMER_BANK_HOLIDAY,
        CHRISTMAS_DAY,
        BOXING_DAY
    );

    @Mock
    private BankHolidaysService bankHolidaysService;

    @Autowired
    private Time time;

    @InjectMocks
    private CalendarService workingDayService;

    @BeforeEach
    void init() {
        when(bankHolidaysService.getBankHolidays()).thenReturn(BANK_HOLIDAYS);
    }

    @Nested
    class IsBankHoliday {

        @Test
        void shouldReturnTrueIfBankHoliday() {
            assertThat(workingDayService.isBankHoliday(GOOD_FRIDAY)).isTrue();
        }

        @Test
        void shouldReturnFalseIfWeekend() {
            assertThat(workingDayService.isBankHoliday(SATURDAY)).isFalse();
            assertThat(workingDayService.isBankHoliday(SUNDAY)).isFalse();
        }

        @Test
        void shouldReturnFalseIfWeekday() {
            assertThat(workingDayService.isBankHoliday(GOOD_FRIDAY.minusDays(1))).isFalse();
        }
    }

    @Nested
    class IsWeekend {

        @Test
        void shouldReturnTrueIfWeekend() {
            assertThat(workingDayService.isWeekend(SATURDAY)).isTrue();
            assertThat(workingDayService.isWeekend(SUNDAY)).isTrue();
        }

        @Test
        void shouldReturnFalseIfBankHoliday() {
            assertThat(workingDayService.isWeekend(GOOD_FRIDAY)).isFalse();
        }

        @Test
        void shouldReturnFalseIfWeekday() {
            assertThat(workingDayService.isWeekend(SUNDAY.plusDays(1))).isFalse();
        }
    }

    @Nested
    class IsWorkingDay {

        @Test
        void shouldReturnTrueIfWorkingDay() {
            assertThat(workingDayService.isWorkingDay(SUNDAY.plusDays(1))).isTrue();
        }

        @Test
        void shouldReturnFalseIfBankHoliday() {
            assertThat(workingDayService.isWorkingDay(GOOD_FRIDAY)).isFalse();
        }

        @Test
        void shouldReturnFalseIfWeekend() {
            assertThat(workingDayService.isWorkingDay(SATURDAY)).isFalse();
            assertThat(workingDayService.isWorkingDay(SATURDAY)).isFalse();
        }
    }

    @Nested
    class WorkingDaysFromDate {

        @Test
        void shouldReturnWorkingDayBeforeBankHoliday() {
            assertThat(workingDayService.getWorkingDayFrom(GOOD_FRIDAY, -2))
                .isEqualTo(LocalDate.of(2020, APRIL, 8));

            assertThat(workingDayService.getWorkingDayFrom(EASTER_MONDAY, -2))
                .isEqualTo(LocalDate.of(2020, APRIL, 8));
        }

        @Test
        void shouldReturnWorkingDayAfterBankHoliday() {
            assertThat(workingDayService.getWorkingDayFrom(GOOD_FRIDAY, 2))
                .isEqualTo(LocalDate.of(2020, APRIL, 15));

            assertThat(workingDayService.getWorkingDayFrom(EASTER_MONDAY, 2))
                .isEqualTo(LocalDate.of(2020, APRIL, 15));
        }

        @Test
        void shouldReturnWorkingDayBeforeWeekend() {
            assertThat(workingDayService.getWorkingDayFrom(GOOD_FRIDAY.plusDays(1), -2))
                .isEqualTo(LocalDate.of(2020, APRIL, 8));

            assertThat(workingDayService.getWorkingDayFrom(GOOD_FRIDAY.plusDays(2), -2))
                .isEqualTo(LocalDate.of(2020, APRIL, 8));
        }

        @Test
        void shouldReturnWorkingDayAfterWeekend() {
            assertThat(workingDayService.getWorkingDayFrom(SATURDAY.plusDays(1), 2))
                .isEqualTo(SATURDAY.plusDays(3));

            assertThat(workingDayService.getWorkingDayFrom(SATURDAY.plusDays(1), 2))
                .isEqualTo(SUNDAY.plusDays(2));
        }

        @Test
        void shouldReturnWorkingDayBeforeMixOfWeekendAndBankHoliday() {
            assertThat(workingDayService.getWorkingDayFrom(EASTER_MONDAY.plusDays(1), -5))
                .isEqualTo(EASTER_MONDAY.minusDays(10));
        }

        @Test
        void shouldReturnWorkingDayAfterMixOfWeekendAndBankHoliday() {
            assertThat(workingDayService.getWorkingDayFrom(GOOD_FRIDAY.minusDays(1), 5))
                .isEqualTo(GOOD_FRIDAY.plusDays(10));
        }

        @Test
        void shouldThrowExceptionWhenNumberOfWorkingDaysIs0() {
            LocalDate now = time.now().toLocalDate();
            assertThrows(IllegalArgumentException.class, () ->
                workingDayService.getWorkingDayFrom(now, 0));
        }
    }
}
