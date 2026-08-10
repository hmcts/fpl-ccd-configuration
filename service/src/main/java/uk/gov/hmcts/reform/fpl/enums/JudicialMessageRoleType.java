package uk.gov.hmcts.reform.fpl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "JudicialMessageRoleTypes", generate = true)
@Getter
@AllArgsConstructor
public enum JudicialMessageRoleType {

    CTSC("CTSC"),

    @CCD(label = "Allocated Judge")
    @JsonProperty("JUDICIARY")
    ALLOCATED_JUDGE("Allocated Judge/Legal Adviser"),

    @CCD(label = "Hearing Judge")
    HEARING_JUDGE("Hearing Judge/Legal Adviser"),

    @CCD(label = "Local Court Admin")
    LOCAL_COURT_ADMIN("Local Court Admin"),

    @CCD(label = "Legal Adviser")
    OTHER("Other Judge/Legal Adviser");

    public final String label;
}
