package uk.gov.hmcts.reform.fpl.service.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.service.DateFormatterService;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;

@Configuration
public class NotificationContentProviderTestConfig {
    @Bean
    @ConditionalOnMissingBean
    public DateFormatterService dateFormatterService() {
        return new DateFormatterService();
    }

    @Bean
    @ConditionalOnMissingBean
    public HearingBookingService hearingBookingService() {
        return new HearingBookingService();
    }
}
