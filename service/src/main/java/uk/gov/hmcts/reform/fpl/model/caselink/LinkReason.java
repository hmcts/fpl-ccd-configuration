package uk.gov.hmcts.reform.fpl.model.caselink;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkReason {

    @JsonProperty(value = "Reason")
    private String reason;

    @JsonProperty(value = "OtherDescription")
    private String otherDescription;
}
