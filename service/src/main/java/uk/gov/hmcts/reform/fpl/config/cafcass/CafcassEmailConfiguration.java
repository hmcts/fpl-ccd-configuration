package uk.gov.hmcts.reform.fpl.config.cafcass;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "cafcass.notification")
public class CafcassEmailConfiguration {
    private String sender;

    @Getter(AccessLevel.NONE)
    private String changeofaddress;

    @Getter(AccessLevel.NONE)
    private String order;

    @Getter(AccessLevel.NONE)
    private String courtbundle;

    @Getter(AccessLevel.NONE)
    private String newapplication;

    @Getter(AccessLevel.NONE)
    private String newdocument;

    @Getter(AccessLevel.NONE)
    private String additionaldocument;

    @Getter(AccessLevel.NONE)
    private String large;

    @Getter(AccessLevel.NONE)
    private String noticeofhearing;

    @NotNull
    private Map<String, String> documentType;

    public String getRecipientForOrder() {
        return order;
    }

    public String getRecipientForCourtBundle() {
        return courtbundle;
    }

    public String getRecipientForNewApplication() {
        return newapplication;
    }

    public String getRecipientForNewDocument() {
        return newdocument;
    }

    public String getRecipientForAdditionlDocument() {
        return additionaldocument;
    }

    public String getRecipientForLargeAttachements() {
        return large;
    }

    public String getRecipientForNoticeOfHearing() {
        return noticeofhearing;
    }

    public String getRecipientForChangeOfAddress() {
        return changeofaddress;
    }
}
