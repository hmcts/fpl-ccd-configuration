package uk.gov.hmcts.reform.ccd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditEvent {

    @JsonProperty("id")
    private final String id;

    @JsonProperty("user_id")
    private final String userId;

    @JsonProperty("user_first_name")
    private final String userFirstName;

    @JsonProperty("user_last_name")
    private final String userLastName;

    @JsonProperty("created_date")
    private final LocalDateTime createdDate;
}
