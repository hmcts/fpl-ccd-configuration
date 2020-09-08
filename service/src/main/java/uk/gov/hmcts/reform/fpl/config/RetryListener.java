package uk.gov.hmcts.reform.fpl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Component
@Slf4j
public class RetryListener extends RetryListenerSupport {

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
                                                 Throwable throwable) {
        log.warn("{} - attempt {} failed.", getOperationName(context), context.getRetryCount());
        super.onError(context, callback, throwable);
    }

    private String getOperationName(RetryContext context) {
        return ofNullable(context.getAttribute("context.name"))
            .map(Object::toString)
            .orElse("Operation");
    }

}
