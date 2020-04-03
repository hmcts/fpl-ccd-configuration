package uk.gov.hmcts.reform.fpl.service.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;

@Configuration
public class LookupTestConfig {

    @Bean
    public HmctsCourtLookupConfiguration courtLookupConfiguration() {
        return new HmctsCourtLookupConfiguration("example=>Family Court:admin@family-court.com:11");
    }

    @Bean
    @ConditionalOnMissingBean
    public CtscEmailLookupConfiguration ctscEmailLookupConfiguration() {
        return new CtscEmailLookupConfiguration("Ctsc+test@gmail.com");
    }

    @Bean
    @ConditionalOnMissingBean
    public CafcassLookupConfiguration cafcassLookupConfiguration() {
        return new CafcassLookupConfiguration("example=>cafcass:FamilyPublicLaw+cafcass@gmail.com");
    }

    @ConditionalOnMissingBean
    @Bean
    public LocalAuthorityNameLookupConfiguration nameLookupConfiguration() {
        return new LocalAuthorityNameLookupConfiguration("example=>Example Local Authority");
    }

    @Bean
    @ConditionalOnMissingBean
    public LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration() {
        return new LocalAuthorityEmailLookupConfiguration("example=>FamilyPublicLaw+sa@gmail.com");
    }
}
