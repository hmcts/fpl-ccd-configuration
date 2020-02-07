package uk.gov.hmcts.reform.fpl.config;

import com.launchdarkly.client.LDClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class LaunchDarklyConfiguration {

    @Bean
    public LDClient ldClient(@Value("${ld.sdk_key}") String sdkKey) {
        return new LDClient(sdkKey);
    }
}
