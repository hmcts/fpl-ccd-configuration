package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Respondent {
    private final RespondentParty party;
    private final String leadRespondentIndicator;
}
