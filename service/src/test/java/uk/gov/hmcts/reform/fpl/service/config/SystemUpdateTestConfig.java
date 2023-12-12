package uk.gov.hmcts.reform.fpl.service.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;

@Configuration
public class SystemUpdateTestConfig {

    @ConditionalOnMissingBean
    @Bean
    public SystemUpdateUserConfiguration systemUpdateUserConfiguration() {
        return new SystemUpdateUserConfiguration("user", "password");
    }
}
