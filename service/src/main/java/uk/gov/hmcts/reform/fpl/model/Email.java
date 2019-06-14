package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

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
