package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Respondents {

    private Respondent firstRespondent;
    private List<AdditionalEntries<Respondent>> additionalRespondents;

    @JsonCreator
    public Respondents(@JsonProperty("firstRespondent") Respondent firstRespondent,
                    @JsonProperty("additionalRespondents") List<AdditionalEntries<Respondent>> additionalRespondents) {
        this.firstRespondent = firstRespondent;
        this.additionalRespondents = additionalRespondents;
    }

    public Respondents(Respondent firstRespondent, Respondent... additionalRespondents) {
        this(firstRespondent, Arrays.stream(additionalRespondents)
            .map(respondent -> new AdditionalEntries<>(UUID.randomUUID(), respondent))
            .collect(Collectors.toList()));
    }

    public Respondent getFirstRespondent() {
        return firstRespondent;
    }

    public List<AdditionalEntries<Respondent>> getAdditionalRespondents() {
        return additionalRespondents;
    }

    public List<Respondent> getAllRespondents() {
        List<Respondent> allRespondents = new ArrayList<>();
        allRespondents.add(firstRespondent);
        additionalRespondents.stream().forEach(additionalEntry -> {
            allRespondents.add(additionalEntry.getValue());
        });
        return allRespondents;
    }

}
