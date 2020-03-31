package uk.gov.hmcts.reform.fpl.config.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Configuration
@ConfigurationProperties("scheduler")
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
public class SchedulerConfiguration {

    private final Map<String, String> quartzConf = new HashMap<>();
    private final DataSourceProperties datasourceConf = new DataSourceProperties();
    private final List<Schedule> schedulerConf = new ArrayList<>();

    @Bean
    public DataSource schedulerDataSource() {
        DataSource dataSource = datasourceConf.initializeDataSourceBuilder().build();
        log.info("TEST--------> {} " + datasourceConf.getPassword());
        migrateDatabase(dataSource);
        return dataSource;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource, JobFactory jobFactory) {
        final Properties properties = new Properties();
        properties.putAll(quartzConf);

        final SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setDataSource(dataSource);
        schedulerFactory.setQuartzProperties(properties);
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.setSchedulerName("Fpl scheduler");

        return schedulerFactory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        scheduler.start();
        return scheduler;
    }

    private void migrateDatabase(DataSource dataSource) {
        Flyway.configure()
            .dataSource(dataSource)
            .load()
            .migrate();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Schedule {
        private String name;
        private String description;
        private Class<? extends Job> jobClass;
        private String cronGroup;
        private String cron;
        private boolean enabled;
    }
}
