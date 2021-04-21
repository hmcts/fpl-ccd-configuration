package uk.gov.hmcts.reform.fnp.client;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fnp.model.payment.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.fnp.model.payment.FeeDto;
import uk.gov.hmcts.reform.fnp.model.payment.enums.Currency;
import uk.gov.hmcts.reform.fnp.model.payment.enums.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "payment_creditAccountPayment", port = "8889")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentConsumerApplication.class})
@TestPropertySource(
    properties = {"fees-register.api.url=localhost:8889", "payment.api.url=localhost:8889"}
)
@PactFolder("pacts")
public class PaymentConsumerTest {
    @Autowired
    PaymentApi paymentApi;
    @Autowired
    ObjectMapper objectMapper;

    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final Long CASE_ID = 12345L;
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    @Pact(provider = "payment_creditAccountPayment", consumer = "fpl_ccdConfiguration")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) throws JSONException, IOException {
        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("accountNumber", "PBA123");
        paymentMap.put("availableBalance", "1000.00");
        paymentMap.put("accountName", "test.account.name");
        return builder
            .given("An active account has sufficient funds for a payment", paymentMap)
            .uponReceiving("A request for payment")
            .path("/credit-account-payments")
            .method("POST")
            .headers(HttpHeaders.AUTHORIZATION, AUTHORIZATION_TOKEN)
            .body(objectMapper.writeValueAsString(getPaymentRequest()))
            .willRespondWith()
            .status(201)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragment")
    public void verifyPaymentSuccess() {
        paymentApi.createCreditAccountPayment(
            AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, getPaymentRequest()
        );
    }

    private static CreditAccountPaymentRequest getPaymentRequest() {
        return CreditAccountPaymentRequest.builder()
            .accountNumber("PBA123")
            .amount(BigDecimal.TEN)
            .caseReference("caseRef")
            .ccdCaseNumber(String.valueOf(CASE_ID))
            .currency(Currency.GBP)
            .customerReference("customerRef")
            .description("FPL Payment")
            .fees(Collections.singletonList(
                FeeDto.builder()
                .calculatedAmount(BigDecimal.TEN)
                .code("test")
                .version(1)
                .build()))
            .organisationName("organisation")
            .service(Service.FPL)
            .siteId("ABA3")
            .build();
    }
}
