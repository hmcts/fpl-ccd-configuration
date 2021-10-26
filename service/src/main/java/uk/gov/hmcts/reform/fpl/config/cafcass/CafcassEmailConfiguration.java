package uk.gov.hmcts.reform.fpl.config.cafcass;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "cafcass.notification")
public class CafcassEmailConfiguration {
    private String sender;
    private String recipient;
}
