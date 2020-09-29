package uk.gov.hmcts.reform.fnp.client;


import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.bouncycastle.util.Arrays;

import java.util.Optional;

public class PaymentFeignCustomDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        final Exception exception = defaultErrorDecoder.decode(methodKey, response);

        final Optional<Retry> retry = Optional.ofNullable(response
            .request()
            .requestTemplate()
            .methodMetadata()
            .method()
            .getAnnotation(Retry.class));

        if (retry.isPresent()) {
            int[] statuses = retry.map(Retry::include).orElse(new int[]{});
            if (Arrays.contains(statuses, response.status())) {
                return new RetryableException(response.status(), response.reason(), response.request().httpMethod(), exception, null, response.request());
            }
        }

        return exception;
    }


}
