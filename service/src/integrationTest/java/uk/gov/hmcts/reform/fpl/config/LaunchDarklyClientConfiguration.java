package uk.gov.hmcts.reform.fpl.config;

import com.launchdarkly.sdk.server.LDClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class LaunchDarklyClientConfiguration {

    @Bean
    @Primary
    public LDClient mockLDClient() {
        return Mockito.mock(LDClient.class);
    }
}

