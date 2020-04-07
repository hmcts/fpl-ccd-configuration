package uk.gov.hmcts.reform.calendar.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.calendar.model.BankHolidays;
import uk.gov.hmcts.reform.fpl.config.FeignConfiguration;

@FeignClient(name = "bank-holidays-api", url = "${bankHolidays.api.url}", configuration = FeignConfiguration.class)
public interface BankHolidaysApi {

    @RequestMapping(method = RequestMethod.GET, value = "/bank-holidays.json")
    BankHolidays retrieveAll();
}
