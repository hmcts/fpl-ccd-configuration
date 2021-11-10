package uk.gov.hmcts.reform.fpl.config.cafcass;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "cafcass.notification")
public class CafcassEmailConfiguration {
    private String sender;

    @Getter(AccessLevel.NONE)
    private String order;

    @Getter(AccessLevel.NONE)
    private String courtbundle;

    @Getter(AccessLevel.NONE)
    private String newApplication;

    public String getRecipientForOrder() {
        return order;
    }

    public String getRecipientForCourtBundle() {
        return courtbundle;
    }

    public String getRecipientForNewApplication() {
        return newApplication;
    }
}
