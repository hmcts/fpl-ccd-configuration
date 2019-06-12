package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Email {
    private final String email;
    private final String emailUsageType;

    @JsonCreator
    public Email(@JsonProperty("email") final String email,
                 @JsonProperty("emailUsageType") final String emailUsageType) {
        this.email = email;
        this.emailUsageType = emailUsageType;
    }
}
