package uk.gov.hmcts.reform.fpl.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.User;

import java.util.Map;

@Data
@EnableConfigurationProperties
@Component
@ConfigurationProperties("test-conf")
public class TestConfiguration {
    private String idamUrl;
    private String fplUrl;
    private Map<String, Object> placeholders;
    private Map<String, User> users;
}
