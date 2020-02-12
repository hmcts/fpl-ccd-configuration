package uk.gov.hmcts.reform.fpl.service.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig;

@Configuration
public class LookupTestConfig {
    @ConditionalOnMissingBean
    @Bean
    public FeesConfig feesConfig() {
        String dummyData = "service=>private law;jurisdiction1=>family;jurisdiction2=>family court;" +
            "event=>miscellaneous;channel=>default;keyword=>KLM";
        return new FeesConfig(
            "http://localhost:8080",
            "/fees-register/fees/lookup",
            dummyData, dummyData,
            dummyData, dummyData,
            dummyData, dummyData,
            dummyData, dummyData,
            dummyData, dummyData);
    }

    @Bean
    public HmctsCourtLookupConfiguration courtLookupConfiguration() {
        return new HmctsCourtLookupConfiguration("example=>Family Court:admin@family-court.com:11");
    }

    @ConditionalOnMissingBean
    @Bean
    public LocalAuthorityNameLookupConfiguration nameLookupConfiguration() {
        return new LocalAuthorityNameLookupConfiguration("example=>Example Local Authority");
    }
}
