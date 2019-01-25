package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalEntries<T> {

    private final UUID id;
    private final T value;

    @JsonCreator
    public AdditionalEntries(@JsonProperty("id") final UUID id,
                             @JsonProperty("value") final T value) {
        this.id = id;
        this.value = value;
    }

}
