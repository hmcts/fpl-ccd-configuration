package uk.gov.hmcts.reform.fpl.service.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_CAFCASS_COURT;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_CAFCASS_EMAIL;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA;
import static uk.gov.hmcts.reform.fpl.Constants.DEFAULT_LA_COURT;

@Configuration
public class LookupTestConfig {

    @ConditionalOnMissingBean
    @Bean
    public HmctsCourtLookupConfiguration courtLookupConfiguration() {
        return new HmctsCourtLookupConfiguration(format("%s=>%s:admin@family-court.com:11", DEFAULT_LA,
            DEFAULT_LA_COURT));
    }

    @Bean
    @ConditionalOnMissingBean
    public CtscEmailLookupConfiguration ctscEmailLookupConfiguration() {
        return new CtscEmailLookupConfiguration("Ctsc+test@gmail.com");
    }

    @Bean
    @ConditionalOnMissingBean
    public CafcassLookupConfiguration cafcassLookupConfiguration() {
        return new CafcassLookupConfiguration(format("%s=>%s:%s", DEFAULT_LA, DEFAULT_CAFCASS_COURT,
            DEFAULT_CAFCASS_EMAIL));
    }

    @ConditionalOnMissingBean
    @Bean
    public LocalAuthorityNameLookupConfiguration nameLookupConfiguration() {
        return new LocalAuthorityNameLookupConfiguration(format("%s=>Example Local Authority", DEFAULT_LA));
    }

    @Bean
    @ConditionalOnMissingBean
    public LocalAuthorityEmailLookupConfiguration emailLookupConfiguration() {
        return new LocalAuthorityEmailLookupConfiguration(format("%s=>FamilyPublicLaw+sa@gmail.com", DEFAULT_LA));
    }
}
