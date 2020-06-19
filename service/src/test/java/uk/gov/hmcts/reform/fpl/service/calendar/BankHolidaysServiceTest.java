package uk.gov.hmcts.reform.fpl.service.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.calendar.client.BankHolidaysApi;
import uk.gov.hmcts.reform.calendar.model.BankHolidays;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class BankHolidaysServiceTest {

    private static final LocalDate BANK_HOLIDAY_1 = LocalDate.of(2020, JANUARY, 1);
    private static final LocalDate BANK_HOLIDAY_2 = LocalDate.of(2020, FEBRUARY, 1);
    private static final BankHolidays BANK_HOLIDAYS = bankHolidaysOf(BANK_HOLIDAY_1, BANK_HOLIDAY_2);

    @Mock
    private BankHolidaysApi bankHolidaysApi;

    @InjectMocks
    private BankHolidaysService bankHolidaysService;

    @Test
    public void shouldFetchBankHolidays() {
        when(bankHolidaysApi.retrieveAll()).thenReturn(BANK_HOLIDAYS);

        final Set<LocalDate> bankHolidays = bankHolidaysService.getBankHolidays();

        assertThat(bankHolidays).containsExactlyInAnyOrder(BANK_HOLIDAY_1, BANK_HOLIDAY_2);
    }

    @Test
    public void shouldCacheBankHolidays() {
        when(bankHolidaysApi.retrieveAll()).thenReturn(BANK_HOLIDAYS);

        final Set<LocalDate> bankHolidays1 = bankHolidaysService.getBankHolidays();
        final Set<LocalDate> bankHolidays2 = bankHolidaysService.getBankHolidays();

        assertThat(bankHolidays1).isEqualTo(bankHolidays2);

        verify(bankHolidaysApi, times(1)).retrieveAll();
    }

    @Test
    public void shouldNotCacheInvalidResposeFormBankHolidayApi() {
        when(bankHolidaysApi.retrieveAll())
            .thenThrow(new RuntimeException())
            .thenReturn(BANK_HOLIDAYS);

        assertThrows(Exception.class, bankHolidaysService::getBankHolidays);

        Set<LocalDate> bankHolidays = bankHolidaysService.getBankHolidays();

        assertThat(bankHolidays).containsExactlyInAnyOrder(BANK_HOLIDAY_1, BANK_HOLIDAY_2);
        verify(bankHolidaysApi, times(2)).retrieveAll();
    }

    private static BankHolidays bankHolidaysOf(LocalDate... dateOfEvent) {
        return BankHolidays.builder().englandAndWales(
            BankHolidays.Division.builder()
                .events(Stream.of(dateOfEvent)
                    .map(date -> BankHolidays.Division.Event.builder().date(date).build())
                    .collect(Collectors.toList()))
                .build()).build();
    }
}
