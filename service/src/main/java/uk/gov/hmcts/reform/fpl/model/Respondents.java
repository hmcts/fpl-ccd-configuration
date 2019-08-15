package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Respondents {
    private final Respondent firstRespondent;
    private final List<Element<Respondent>> additional;

    @JsonCreator
    public Respondents(@JsonProperty("firstRespondent") Respondent firstRespondent,
                       @JsonProperty("additional") List<Element<Respondent>> additional) {
        this.firstRespondent = firstRespondent;
        this.additional = additional;
    }

    public Respondents(Respondent firstRespondent, Respondent... additional) {
        this(firstRespondent, Arrays.stream(additional)
            .map(respondent -> new Element<>(UUID.randomUUID(), respondent))
            .collect(Collectors.toList()));
    }

    @JsonIgnore
    public List<Respondent> getAllRespondents() {
        ImmutableList.Builder<Respondent> builder = ImmutableList.builder();
        if (firstRespondent != null) {
            builder.add(firstRespondent);
        }
        if (additional != null) {
            builder.addAll(additional.stream().map(Element::getValue).collect(Collectors.toList()));
        }
        return builder.build();
    }
}
