package uk.gov.hmcts.reform.fpl.model.caseflag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaseFlag {
    @JsonProperty("partyName")
    private String partyName;
    @JsonProperty("roleOnCase")
    private String roleOnCase;
    @JsonProperty("details")
    private List<FlagDetailData> details;
}
