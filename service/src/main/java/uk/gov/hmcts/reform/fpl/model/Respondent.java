package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
public class Respondent {
    @Valid
    @NotNull(message = "You need to add details to respondents")
    private final RespondentParty party;
    private final String leadRespondentIndicator;
}
