package uk.gov.hmcts.reform.fpl.config.feign.codec;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

public class IdamErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder;

    public IdamErrorDecoder() {
        this(new Default());
    }

    public IdamErrorDecoder(ErrorDecoder defaultDecoder) {
        this.defaultDecoder = defaultDecoder;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        Exception exception = defaultDecoder.decode(methodKey, response);

        if (exception instanceof RetryableException) {
            return exception;
        }

        if (HttpStatus.valueOf(response.status()).is5xxServerError()) {
            return new RetryableException(
                response.status(),
                response.reason(),
                response.request().httpMethod(),
                exception,
                0l,
                response.request()
            );
        }

        return exception;
    }
}
