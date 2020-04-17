package uk.gov.hmcts.reform.fpl.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.lang.reflect.Method;

@Configuration
public class AsyncConfiguration implements AsyncConfigurer {

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
            logger.error("Unexpected error occurred during async execution", throwable);
        }
    }

}
