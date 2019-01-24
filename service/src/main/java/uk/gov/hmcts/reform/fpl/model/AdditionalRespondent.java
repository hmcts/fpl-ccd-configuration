package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalRespondent {

    private UUID id;
    private Respondent respondent;

    @JsonCreator
    public AdditionalRespondent(@JsonProperty("id") final UUID id,
                              @JsonProperty("value") final Respondent respondent) {
        this.id = id;
        this.respondent = respondent;
    }

    public UUID getId() {
        return id;
    }

    public Respondent getRespondent() {
        return respondent;
    }

}
