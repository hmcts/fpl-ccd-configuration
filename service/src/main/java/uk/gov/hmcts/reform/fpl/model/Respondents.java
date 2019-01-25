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
    private final List<Element<Respondent>> additionalRespondents;

    @JsonCreator
    public Respondents(@JsonProperty("firstRespondent") Respondent firstRespondent,
                       @JsonProperty("additionalRespondents") List<Element<Respondent>> additionalRespondents) {
        this.firstRespondent = firstRespondent;
        this.additionalRespondents = additionalRespondents;
    }

    public Respondents(Respondent firstRespondent, Respondent... additionalRespondents) {
        this(firstRespondent, Arrays.stream(additionalRespondents)
            .map(respondent -> new Element<>(UUID.randomUUID(), respondent))
            .collect(Collectors.toList()));
    }

    @JsonIgnore
    public List<Respondent> getAllRespondents() {
        ImmutableList.Builder<Respondent> builder = ImmutableList.builder();
        if (firstRespondent != null) {
            builder.add(firstRespondent);
        }
        if (additionalRespondents != null) {
            builder.addAll(additionalRespondents.stream().map(Element::getValue).collect(Collectors.toList()));
        }
        return builder.build();
    }

}
