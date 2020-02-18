package uk.gov.hmcts.reform.fpl.testbeans;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.fnp.model.fee.FeeType;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeParameters;

import java.util.Arrays;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

public class TestFeeConfig {
    @ConditionalOnMissingBean
    @Bean
    public static FeesConfig feesConfig() {
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
}
