package uk.gov.hmcts.reform.fpl.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
        logger.error("Unexpected error occurred during async execution: " + throwable.getMessage(), throwable);
    }

}
