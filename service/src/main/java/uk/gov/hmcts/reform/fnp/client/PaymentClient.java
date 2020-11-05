package uk.gov.hmcts.reform.fnp.client;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fnp.exception.PaymentsApiException;
import uk.gov.hmcts.reform.fnp.exception.RetryablePaymentException;
import uk.gov.hmcts.reform.fnp.model.payment.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.fpl.request.RequestData;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentClient {

    private final PaymentApi paymentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final RequestData requestData;

    @Retryable(value = {RetryablePaymentException.class}, label = "payment api call")
    public void callPaymentsApi(CreditAccountPaymentRequest creditAccountPaymentRequest) {
        try {
            paymentApi.createCreditAccountPayment(requestData.authorisation(),
                authTokenGenerator.generate(),
                creditAccountPaymentRequest);
        } catch (FeignException.InternalServerError ex) {
            throw new RetryablePaymentException(ex.contentUTF8(), ex);
        } catch (FeignException ex) {
            log.error("Payments response error for {}\n\tstatus: {} => message: \"{}\"",
                creditAccountPaymentRequest, ex.status(), ex.contentUTF8(), ex);
            log.info("Feign exception caught, payment will not be retried");
            throw new PaymentsApiException(ex.contentUTF8(), ex);
        }
    }
}
