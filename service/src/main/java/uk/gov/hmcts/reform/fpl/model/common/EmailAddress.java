package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailAddress {
    private final String email;
    private final String emailUsageType;

    @JsonCreator
    private EmailAddress(@JsonProperty("email") String email, String emailUsageType) {
        this.email = email;
        this.emailUsageType = emailUsageType;
    }
}
