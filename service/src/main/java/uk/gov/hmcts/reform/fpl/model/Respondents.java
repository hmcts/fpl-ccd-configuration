package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Respondents {

    private Respondent firstRespondent;
    private List<AdditionalRespondent> additionalRespondents;

    @JsonCreator
    public Respondents(@JsonProperty("firstRespondent") final Respondent firstRespondent,
                    @JsonProperty("additional") final List<AdditionalRespondent> additionalRespondents) {
        this.firstRespondent = firstRespondent;
        this.additionalRespondents = additionalRespondents;
    }

    public Respondent getFirstRespondent() {
        return firstRespondent;
    }

    public List<AdditionalRespondent> getAdditionalRespondents() {
        return additionalRespondents;
    }

}
