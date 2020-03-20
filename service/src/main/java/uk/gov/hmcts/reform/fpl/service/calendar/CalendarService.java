package uk.gov.hmcts.reform.fpl.service.calendar;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Service
public class CalendarService {

    private final BankHolidaysService bankHolidaysService;

    public CalendarService(BankHolidaysService bankHolidaysService) {
        this.bankHolidaysService = bankHolidaysService;
    }

    public boolean isWorkingDay(LocalDate date) {
        return !isWeekend(date) && !isBankHoliday(date);
    }

    private boolean isWorkingDay(LocalDate date, Set<LocalDate> bankHolidays) {
        return !isWeekend(date) && !bankHolidays.contains(date);
    }

    public boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    public boolean isBankHoliday(LocalDate date) {
        return bankHolidaysService.getBankHolidays().contains(date);
    }

    public LocalDate getWorkingDayFrom(LocalDate fromDate, int numberOfWorkingDays) {

        if (numberOfWorkingDays == 0) {
            throw new IllegalArgumentException("Number of working days must not be 0.");
        }

        Set<LocalDate> bankHolidays = bankHolidaysService.getBankHolidays();

        int dayIncrement = Integer.signum(numberOfWorkingDays);
        int remainingWorkingDaysToFind = Math.abs(numberOfWorkingDays);

        LocalDate dateCandidate = fromDate;

        while (remainingWorkingDaysToFind > 0) {
            dateCandidate = dateCandidate.plusDays(dayIncrement);
            if (isWorkingDay(dateCandidate, bankHolidays)) {
                remainingWorkingDaysToFind--;
            }
        }

        return dateCandidate;
    }

}
