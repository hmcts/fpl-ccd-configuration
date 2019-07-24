package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Child {
    private final ChildParty party;

    @JsonCreator
    public Child(@JsonProperty("party") ChildParty party) {
        this.party = party;
    }
}
