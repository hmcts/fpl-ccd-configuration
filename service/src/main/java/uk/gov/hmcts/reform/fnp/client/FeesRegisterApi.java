package uk.gov.hmcts.reform.fnp.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.fnp.model.fee.FeeResponse;
import uk.gov.hmcts.reform.fpl.config.FeignConfiguration;

@FeignClient(name = "fees-register-api", url = "${fee.url}", configuration = FeignConfiguration.class)
public interface FeesRegisterApi {
    @GetMapping("/fees-register/fees/lookup")
    FeeResponse findFee(
        @RequestParam(name = "channel") final String channel,
        @RequestParam(name = "event") final String event,
        @RequestParam(name = "jurisdiction1") final String jurisdiction1,
        @RequestParam(name = "jurisdiction2") final String jurisdiction2,
        @RequestParam(name = "keyword", required = false) final String keyword,
        @RequestParam(name = "service") final String service
    );
}
