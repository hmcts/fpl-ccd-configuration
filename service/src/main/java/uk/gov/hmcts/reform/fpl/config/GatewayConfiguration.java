package uk.gov.hmcts.reform.fpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class GatewayConfiguration {
    private final String gatewayUrl;

    public GatewayConfiguration(@Value("${document_management.gateway_url}") String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }
}
