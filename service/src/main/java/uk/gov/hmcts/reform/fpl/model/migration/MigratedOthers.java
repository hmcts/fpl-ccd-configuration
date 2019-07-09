package uk.gov.hmcts.reform.fpl.model.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.OtherParty;

@Data
@Builder
@AllArgsConstructor
public class MigratedOthers {
    private final OtherParty party;
}
