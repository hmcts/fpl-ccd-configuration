package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Respondents {
    private final RespondentParty party;
    private final String leadRespondentIndicator;
}
