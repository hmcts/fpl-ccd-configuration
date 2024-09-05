package uk.gov.hmcts.reform.fpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CafcassSystemUpdateUserConfiguration {
    private final String userName;

    public CafcassSystemUpdateUserConfiguration(@Value("${fpl.cafcass_system_update.username}") String userName) {
        this.userName = userName;
    }
}
