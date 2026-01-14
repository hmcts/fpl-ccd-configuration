package uk.gov.hmcts.reform.fpl.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SyncTaskExecutor;

@TestConfiguration
public class EventListenerTestConfiguration {

    @Primary
    @Bean
    public SimpleApplicationEventMulticaster applicationEventMulticaster() {
        // force all async event listener to execute synchronously for tests
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SyncTaskExecutor());
        return eventMulticaster;
    }
}
