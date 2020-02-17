package uk.gov.hmcts.reform.fpl.config.payment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fnp.model.fee.FeeType;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties("fee")
public class FeesConfig {
    private Map<FeeType, FeeParameters> parameters;

    public FeeParameters getFeeParametersByFeeType(FeeType feeType) {
        return parameters.get(feeType);
    }

    @Getter
    @Setter
    @ToString
    public static class FeeParameters {
        private String channel;
        private String event;
        private String jurisdiction1;
        private String jurisdiction2;
        private String keyword;
        private String service;
    }

}
