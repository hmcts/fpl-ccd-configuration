package uk.gov.hmcts.reform.fpl.model.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.PartyExtended;

@Data
@Builder
@AllArgsConstructor
public class MigratedRespondent {
    @JsonProperty("party")
    private final PartyExtended party;
    private final String leadRespondentIndicator;
}
