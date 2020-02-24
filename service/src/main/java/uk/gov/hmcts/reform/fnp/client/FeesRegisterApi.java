package uk.gov.hmcts.reform.fnp.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.fnp.model.fee.FeeResponse;
import uk.gov.hmcts.reform.fpl.config.FeignConfiguration;

@FeignClient(name = "fees-register-api", url = "${fees_register.api.url}", configuration = FeignConfiguration.class)
public interface FeesRegisterApi {
    @GetMapping("/fees-register/fees/lookup")
    FeeResponse findFee(
        @RequestParam(name = "channel") String channel,
        @RequestParam(name = "event") String event,
        @RequestParam(name = "jurisdiction1") String jurisdiction1,
        @RequestParam(name = "jurisdiction2") String jurisdiction2,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "service") String service
    );
}
