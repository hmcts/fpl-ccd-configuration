package uk.gov.hmcts.reform.fpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CafcassSystemUpdateUserConfiguration {
    private final String userName;
    private final String password;

    public CafcassSystemUpdateUserConfiguration(@Value("${fpl.cafcass_system_update.username}") String userName,
                                                @Value("${fpl.cafcass_system_update.password}") String password) {
        this.userName = userName;
        this.password = password;
    }
}
