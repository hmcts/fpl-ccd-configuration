package uk.gov.hmcts.reform.fpl.model.caselink;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseLink {

    @JsonProperty(value = "CaseReference")
    private final String caseReference;

    @JsonProperty("ReasonForLink")
    private final List<LinkReason> reasonForLink;

    @JsonProperty("CreatedDateTime")
    private final LocalDateTime createdDateTime;

    @JsonProperty(value = "CaseType")
    private final String caseType;
}
