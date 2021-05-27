package uk.gov.hmcts.reform.fnp.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.fnp.model.payment.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.fnp.model.payment.Payments;
import uk.gov.hmcts.reform.fpl.config.feign.FeignClientConfiguration;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(name = "payment-api", url = "${payment.api.url}", configuration = FeignClientConfiguration.class)
public interface PaymentApi {

    @PostMapping("/credit-account-payments")
    CreditAccountPaymentRequest createCreditAccountPayment(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody CreditAccountPaymentRequest creditAccountPaymentRequest
    );

    @GetMapping("/cases/{caseId}/payments")
    Payments getCasePayments(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable("caseId") String caseId
    );

}
