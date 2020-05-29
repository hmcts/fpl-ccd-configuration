package uk.gov.hmcts.reform.fpl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;

@Slf4j
@Configuration
public class AsyncConfiguration implements AsyncConfigurer {

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
        taskExecutor.setTaskDecorator(new AsyncTaskDecorator());
        return taskExecutor;
    }

    static class AsyncTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(@Nonnull Runnable task) {
            RequestAttributes context = RequestContextHolder.currentRequestAttributes();
            return () -> {
                try {
                    RequestContextHolder.setRequestAttributes(context);
                    task.run();
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        }
    }
}
