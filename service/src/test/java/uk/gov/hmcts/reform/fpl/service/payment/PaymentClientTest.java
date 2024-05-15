package uk.gov.hmcts.reform.fpl.service.payment;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fnp.client.PaymentApi;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fnp.model.payment.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.fnp.model.payment.FeeDto;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Currency.GBP;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Service.FPL;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentClient.class})
@EnableRetry
class PaymentClientTest {

    private static final String SERVICE_AUTH_TOKEN = "servicetoken";
    private static final String AUTH_TOKEN = "token";
    private static final Long CASE_ID = 1L;

    @Autowired
    private PaymentClient paymentClient;

    @MockBean
    private PaymentApi paymentApi;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private RequestData requestData;

    @BeforeEach
    void setup() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(requestData.authorisation()).thenReturn(AUTH_TOKEN);
    }

    FeeDto fee = FeeDto.builder().calculatedAmount(BigDecimal.TEN).build();

    @Test
    void shouldRetryPaymentsApiWhenInternalServerErrorThrown() {
        CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
            .customerReference("1")
            .amount(fee.getCalculatedAmount())
            .fees(List.of(fee))
            .build();

        when(paymentApi.createCreditAccountPayment(AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedPaymentRequest))
            .thenThrow(FeignException.InternalServerError.class);


        assertThrows(PaymentsApiException.class, () -> paymentClient.callPaymentsApi(expectedPaymentRequest));
        verify(paymentApi, times(3)).createCreditAccountPayment(AUTH_TOKEN,
            SERVICE_AUTH_TOKEN, expectedPaymentRequest);
    }

    @Test
    void shouldNotRetryPaymentsApiWhenExceptionOtherThanInternalServerIsThrown() {
        CreditAccountPaymentRequest paymentRequest = testCreditAccountPaymentRequestBuilder()
            .customerReference("1")
            .amount(fee.getCalculatedAmount())
            .fees(List.of(fee))
            .build();

        when(paymentApi.createCreditAccountPayment(AUTH_TOKEN, SERVICE_AUTH_TOKEN, paymentRequest))
            .thenThrow(FeignException.NotImplemented.class);

        assertThrows(PaymentsApiException.class, () -> paymentClient.callPaymentsApi(paymentRequest));
        verify(paymentApi).createCreditAccountPayment(AUTH_TOKEN,
            SERVICE_AUTH_TOKEN, paymentRequest);
    }

    @Test
    void shouldFailOnPaymentsApiOnceThenHaveSuccessfulRetry() {
        CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
            .customerReference("1")
            .amount(fee.getCalculatedAmount())
            .fees(List.of(fee))
            .build();

        when(paymentApi.createCreditAccountPayment(AUTH_TOKEN, SERVICE_AUTH_TOKEN, expectedPaymentRequest))
            .thenThrow(FeignException.InternalServerError.class)
            .thenReturn(expectedPaymentRequest);

        paymentClient.callPaymentsApi(expectedPaymentRequest);
        verify(paymentApi, times(2)).createCreditAccountPayment(AUTH_TOKEN,
            SERVICE_AUTH_TOKEN, expectedPaymentRequest);
    }

    @Test
    void shouldReturnPaymentsApiExceptionOnFeignException() {
        String responseBodyContent = "Response message";
        CreditAccountPaymentRequest paymentRequest = testCreditAccountPaymentRequestBuilder()
            .customerReference("1")
            .amount(fee.getCalculatedAmount())
            .fees(List.of(fee))
            .build();

        when(paymentApi.createCreditAccountPayment(any(), any(), any())).thenThrow(
            new FeignException.UnprocessableEntity("",
                Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
                responseBodyContent.getBytes(), Collections.emptyMap()));

        assertThatThrownBy(() -> paymentClient.callPaymentsApi(paymentRequest))
            .isInstanceOf(PaymentsApiException.class)
            .hasMessage(responseBodyContent);
    }

    private CreditAccountPaymentRequest.CreditAccountPaymentRequestBuilder testCreditAccountPaymentRequestBuilder() {
        return CreditAccountPaymentRequest.builder()
            .accountNumber("PBA123")
            .caseReference("clientCode")
            .ccdCaseNumber(String.valueOf(CASE_ID))
            .currency(GBP)
            .description("Payment for case: " + CASE_ID)
            .organisationName("Example Local Authority")
            .service(FPL)
            .siteId("TEST_SITE_ID");
    }
}
