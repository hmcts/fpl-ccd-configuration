package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.fpl.model.interfaces.Representable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class Respondent extends Representable {
    @Valid
    @NotNull(message = "You need to add details to respondents")
    private final RespondentParty party;
    private final String leadRespondentIndicator;
}
