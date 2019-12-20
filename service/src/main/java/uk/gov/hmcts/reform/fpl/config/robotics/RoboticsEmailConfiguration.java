package uk.gov.hmcts.reform.fpl.config.robotics;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "robotics.notification")
public class RoboticsEmailConfiguration {
    private String sender;
    private String recipient;
}
