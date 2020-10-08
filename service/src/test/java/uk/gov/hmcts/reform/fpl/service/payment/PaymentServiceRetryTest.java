package uk.gov.hmcts.reform.fpl.service.payment;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fnp.client.PaymentApi;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fnp.model.payment.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.fnp.model.payment.FeeDto;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Currency.GBP;
import static uk.gov.hmcts.reform.fnp.model.payment.enums.Service.FPL;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentService.class})
class PaymentServiceRetryTest {

    @Autowired
    @Qualifier("PaymentApi")
    private PaymentApi paymentApi;

    @MockBean
    private FeeService feeService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private RequestData requestData;

    @MockBean
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    private PaymentService paymentService;

    private static final Long CASE_ID = 1L;
    FeeDto feeForC2WithNotice = FeeDto.builder().calculatedAmount(BigDecimal.TEN).build();

    @BeforeEach
    void setup() {
        paymentService = new PaymentService(feeService, paymentApi, authTokenGenerator, requestData, localAuthorityNameLookupConfiguration,
            "TEST_SITE_ID");
        when(feeService.getFeesDataForC2(WITH_NOTICE)).thenReturn(buildFeesData(feeForC2WithNotice));
    }

    @Test
    public void testRetry() {

        CreditAccountPaymentRequest expectedPaymentRequest = testCreditAccountPaymentRequestBuilder()
            .customerReference("1")
            .amount(feeForC2WithNotice.getCalculatedAmount())
            .fees(List.of(feeForC2WithNotice))
            .build();

        Mockito.doThrow(FeignException.InternalServerError.class).when(paymentApi).createCreditAccountPayment(
            any(), any(), any());

        try {
            paymentService.callPaymentsApi(expectedPaymentRequest);
        } catch (PaymentsApiException ex) {
        } finally {
            verify(paymentApi, times(3)).createCreditAccountPayment(any(), any(), any());

        }
    }

    @Configuration
    @EnableRetry
    //without this it wouldn't let me autowire the payment api interface
    public static class SpringConfig {
        @Bean(name = "PaymentApi")
        public PaymentApi paymentApi() {
            PaymentApi paymentApi = mock(PaymentApi.class);
            when(paymentApi.createCreditAccountPayment(any(), any(), any()))
                .thenThrow(FeignException.InternalServerError.class)
                .thenThrow(FeignException.InternalServerError.class);
            return paymentApi;
        }
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

    private FeesData buildFeesData(FeeDto feeDto) {
        return FeesData.builder()
            .totalAmount(feeDto.getCalculatedAmount())
            .fees(List.of(feeDto))
            .build();
    }

}
