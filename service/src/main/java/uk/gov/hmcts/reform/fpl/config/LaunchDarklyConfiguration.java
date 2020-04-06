package uk.gov.hmcts.reform.fpl.config;

import com.launchdarkly.client.LDClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class LaunchDarklyConfiguration {

    @Bean
    public LDClient ldClient(@Value("${ld.sdk_key}") String sdkKey) {
        var ldClient = new LDClient(sdkKey);
        log.error("LD SDK key: " + sdkKey);
        if (!ldClient.initialized()) {
            //throw new RuntimeException("LaunchDarkly initialization failed.");
        }

        return ldClient;
    }
}
