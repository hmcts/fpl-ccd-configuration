package uk.gov.hmcts.reform.fpl.config.robotics;

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
@ConfigurationProperties(prefix = "robotics.notification")
public class RoboticsEmailConfiguration {
    private String sender;
    private String recipient;
}
