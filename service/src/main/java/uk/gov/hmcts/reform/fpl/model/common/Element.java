package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Element<T> {
    private final UUID id;
    @NotNull
    @Valid
    private final T value;

    @JsonCreator
    public Element(@JsonProperty("id") final UUID id,
                   @JsonProperty("value") final T value) {
        this.id = id;
        this.value = value;
    }
}
