package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MigratedChildren {
    private final ChildParty party;

    @JsonCreator
    private MigratedChildren(@JsonProperty("party") ChildParty party) {
        this.party = party;
    }
}
