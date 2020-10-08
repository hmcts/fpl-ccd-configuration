package uk.gov.hmcts.reform.fpl.service.payment;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fnp.client.PaymentApi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
class PaymentServiceRetryTest {

    @Autowired
    @Qualifier("PaymentApi")
    private PaymentApi paymentApi;

    @Test
    public void testRetry() {
        Mockito.doThrow(FeignException.InternalServerError.class).when(paymentApi).createCreditAccountPayment(
            any(), any(), any());

        try {
            paymentApi.createCreditAccountPayment(any(), any(), any());
        } catch (FeignException.InternalServerError ex) {
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
}
