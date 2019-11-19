package uk.gov.hmcts.reform.fpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class GatewayConfiguration {
    private final String url;

    public GatewayConfiguration(@Value("${gateway.url}") String url) {
        this.url = url;
    }
}
