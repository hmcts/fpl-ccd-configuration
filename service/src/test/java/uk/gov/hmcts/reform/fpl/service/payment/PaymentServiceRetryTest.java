package uk.gov.hmcts.reform.fpl.service.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    @Qualifier("paymentApi")
    private PaymentApi paymentApi;

    @Test
    public void testRetry() {

        try {
            paymentApi.createCreditAccountPayment(any(), any(), any());
        } catch(Exception ex) {
            System.out.println("Caught " + ex.getStackTrace());
        }

        verify(paymentApi, times(2)).createCreditAccountPayment(any(), any(), any());
    }

    @Configuration
    @EnableRetry
    //without this it wouldn't let me autowire the payment api interface
    public static class SpringConfig {
        @Bean(name = "paymentApi")
        public PaymentApi paymentApi() throws Exception {
            PaymentApi remoteService = mock(PaymentApi.class);
            when(remoteService.createCreditAccountPayment(any(), any(), any()))
                .thenThrow(new RuntimeException("Remote Exception 1"))
                .thenThrow(new RuntimeException("Remote Exception 2"));
            return remoteService;
        }
    }

}
