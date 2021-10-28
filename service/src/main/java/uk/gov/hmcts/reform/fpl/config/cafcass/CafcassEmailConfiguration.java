package uk.gov.hmcts.reform.fpl.config.cafcass;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
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

    public String getRecipientForOrder() {
        return order;
    }

    public String getRecipientForCourtBundle() {
        return courtbundle;
    }
}
