package uk.gov.hmcts.reform.fpl.service.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.calendar.client.BankHolidaysApi;
import uk.gov.hmcts.reform.calendar.model.BankHolidays;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.google.common.base.Suppliers.memoizeWithExpiration;
import static java.util.stream.Collectors.toSet;


@Service
public class BankHolidaysService {

    private static final int CACHE_HOURS = 12;

    private Supplier<Set<LocalDate>> bankHolidays;

    @Autowired
    public BankHolidaysService(BankHolidaysApi bankHolidaysApi) {
        bankHolidays = memoizeWithExpiration(() -> fetchBankHolidays(bankHolidaysApi), CACHE_HOURS, TimeUnit.HOURS);
    }

    public Set<LocalDate> getBankHolidays() {
        return bankHolidays.get();
    }

    private Set<LocalDate> fetchBankHolidays(final BankHolidaysApi bankHolidaysApi) {
        return bankHolidaysApi.retrieveAll().getEnglandAndWales().getEvents().stream()
            .map(BankHolidays.Division.Event::getDate)
            .collect(toSet());
    }
}
