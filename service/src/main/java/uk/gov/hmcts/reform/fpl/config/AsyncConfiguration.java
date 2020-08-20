package uk.gov.hmcts.reform.fpl.config;

import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.request.RequestDataCache;
import uk.gov.hmcts.reform.fpl.request.SimpleRequestData;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AsyncConfiguration implements AsyncConfigurer {

    private final ApplicationContext context;
    private final HttpServletRequest httpServletRequest;

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
            log.error("Unexpected error occurred during async execution", throwable);
        }
    }

    @Override
    @Bean
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setTaskDecorator(new AsyncTaskDecorator(context, httpServletRequest));
        return taskExecutor;
    }

    static class AsyncTaskDecorator implements TaskDecorator {

        final ApplicationContext context;
        final HttpServletRequest httpServletRequest;

        AsyncTaskDecorator(ApplicationContext context, HttpServletRequest httpServletRequest) {
            this.context = context;
            this.httpServletRequest = httpServletRequest;
        }

        @Override
        public Runnable decorate(@Nonnull Runnable task) {
            SimpleRequestData requestData = new SimpleRequestData(context.getBean(RequestData.class));
            RequestTelemetryContext context = ThreadContext.getRequestTelemetryContext();
            return () -> {
                RequestDataCache.add(requestData);
                try {
                    ThreadContext.setRequestTelemetryContext(context);
                    try {
                        log.warn("TOMEK TEST IDY " + context.getHttpRequestTelemetry()
                            .getContext().getOperation().getId());
                    } catch (Exception e) {
                        log.error("EE", e);
                    }
                    task.run();
                } finally {
                    //ac.complete();
                    RequestDataCache.remove();
                }
            };
        }
    }
}
