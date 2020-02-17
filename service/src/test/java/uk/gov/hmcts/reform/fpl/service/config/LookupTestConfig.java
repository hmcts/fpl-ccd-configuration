package uk.gov.hmcts.reform.fpl.service.config;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fnp.model.fee.FeeType;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeParameters;

import java.util.Arrays;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@Configuration
public class LookupTestConfig {
    @ConditionalOnMissingBean
    @Bean
    public FeesConfig feesConfig() {
        FeeParameters parameters = new FeeParameters(
            "default",
            "miscellaneous",
            "family",
            "family court",
            "KLM",
            "private law"
        );

        FeesConfig config = new FeesConfig();

        config.setParameters(Arrays.stream(FeeType.values())
            .collect(collectingAndThen(toMap(feeType -> feeType, ignored -> parameters), ImmutableMap::copyOf)));

        return config;
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
