package uk.gov.hmcts.reform.fpl.config.scheduler;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.stereotype.Component;


/**
 * Enables DI in classes bootstrapped by quartz.
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
public class JobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

    private final ApplicationContext applicationContext;

    @NotNull
    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        final Object job = super.createJobInstance(bundle);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(job);
        return job;
    }
}
