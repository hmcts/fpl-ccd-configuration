package uk.gov.hmcts.reform.fpl.model.caseflag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseFlagsType {
    @JsonProperty("partyName")
    private String partyName;
    @JsonProperty("roleOnCase")
    private String roleOnCase;
    @JsonProperty("groupId")
    private String groupId;
    @JsonProperty("visibility")
    private String visibility;
    @JsonProperty("details")
    private ListTypeItem<FlagDetailType> details;
}
