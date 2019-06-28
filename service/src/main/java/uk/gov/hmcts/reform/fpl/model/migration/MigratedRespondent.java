package uk.gov.hmcts.reform.fpl.model.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;

@Data
@Builder
@AllArgsConstructor
public class MigratedRespondent {
    private final RespondentParty party;
    private final String leadRespondentIndicator;
}
