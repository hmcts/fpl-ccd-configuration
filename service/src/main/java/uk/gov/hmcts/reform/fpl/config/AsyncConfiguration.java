package uk.gov.hmcts.reform.fpl.config;

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

@Slf4j
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AsyncConfiguration implements AsyncConfigurer {

    private final ApplicationContext context;

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
        taskExecutor.setTaskDecorator(new AsyncTaskDecorator(context));
        return taskExecutor;
    }

    static class AsyncTaskDecorator implements TaskDecorator {

        final ApplicationContext context;

        AsyncTaskDecorator(ApplicationContext context) {
            this.context = context;
        }

        @Override
        public Runnable decorate(@Nonnull Runnable task) {
            SimpleRequestData requestData = new SimpleRequestData(context.getBean(RequestData.class));

            return () -> {
                RequestDataCache.add(requestData);
                try {
                    task.run();
                } finally {
                    RequestDataCache.remove();
                }
            };
        }
    }
}
