package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JudicialMessageRoleType {

    CTSC("CTSC"),

    @JsonProperty("JUDICIARY")
    ALLOCATED_JUDGE("Allocated Judge/Legal Adviser"),

    HEARING_JUDGE("Hearing Judge/Legal Adviser"),

    LOCAL_COURT_ADMIN("Local Court Admin"),

    OTHER("Other Judge");

    public final String label;
}
