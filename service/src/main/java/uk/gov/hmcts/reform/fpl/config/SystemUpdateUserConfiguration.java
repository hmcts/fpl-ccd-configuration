package uk.gov.hmcts.reform.fpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SystemUpdateUserConfiguration {
    private final String userName;
    private final String password;
    private final boolean cacheTokenEnabled;

    public SystemUpdateUserConfiguration(@Value("${fpl.system_update.username}") String userName,
                                         @Value("${fpl.system_update.password}") String password,
                                         @Value("${system_user_service.cache:false}") boolean cacheTokenEnabled) {
        this.userName = userName;
        this.password = password;
        this.cacheTokenEnabled = cacheTokenEnabled;
    }
}
