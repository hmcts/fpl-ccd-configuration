package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalEntries<T> {

    private UUID id;
    private T value;

    @JsonCreator
    public AdditionalEntries(@JsonProperty("id") final UUID id,
                           @JsonProperty("value") final T value) {
        this.id = id;
        this.value = value;
    }

    public UUID getId() {
        return id;
    }

    public T getValue() {
        return value;
    }

}
