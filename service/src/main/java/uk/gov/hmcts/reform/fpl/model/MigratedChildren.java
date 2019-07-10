package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MigratedChildren {
    private final ChildParty party;

    @JsonCreator
    private MigratedChildren(ChildParty party) {
        this.party = party;
    }
}
