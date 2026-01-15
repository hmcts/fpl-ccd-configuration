package uk.gov.hmcts.reform.fpl.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@TestConfiguration
public class EventListenerTestConfiguration {

    @Primary
    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster(@Autowired TaskExecutor taskExecutor) {
        // force all async event listener to execute synchronously for tests
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(taskExecutor);
        return eventMulticaster;
    }

    @Primary
    @Bean
    public TaskExecutor taskExecutor() {
        return  new SyncTaskExecutor();
    }
}
