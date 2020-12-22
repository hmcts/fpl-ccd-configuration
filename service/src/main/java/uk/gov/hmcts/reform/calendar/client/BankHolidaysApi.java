package uk.gov.hmcts.reform.calendar.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmcts.reform.calendar.model.BankHolidays;
import uk.gov.hmcts.reform.fpl.config.feign.FeignClientConfiguration;

@FeignClient(name = "bank-holidays-api", url = "${bankHolidays.api.url}", configuration = FeignClientConfiguration.class)
public interface BankHolidaysApi {

    @GetMapping(value = "/bank-holidays.json")
    BankHolidays retrieveAll();
}
