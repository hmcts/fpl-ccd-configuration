package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Applicant {
    @JsonProperty("party")
    private final PartyApplicant party;
    private final String leadApplicantIndicator;
}
