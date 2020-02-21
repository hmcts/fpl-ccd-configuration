package uk.gov.hmcts.reform.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.fpl.config.FeignConfiguration;
import uk.gov.hmcts.reform.payment.model.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.rd.model.Status;
import uk.gov.hmcts.reform.rd.model.User;
import uk.gov.hmcts.reform.rd.model.Users;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(name = "payment-api", url = "${payment.api.url}", configuration = FeignConfiguration.class)
public interface PaymentApi {
    @PostMapping("/credit-account-payments")
    CreditAccountPaymentRequest createCreditAccountPayment(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody CreditAccountPaymentRequest creditAccountPaymentRequest
    );

}
