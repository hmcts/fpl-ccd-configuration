package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalChild {

    private UUID id;
    private Child child;

    @JsonCreator
    public AdditionalChild(@JsonProperty("id") final UUID id,
                           @JsonProperty("value") final Child child) {
        this.id = id;
        this.child = child;
    }

    public UUID getId() {
        return id;
    }

    public Child getChild() {
        return child;
    }

}
