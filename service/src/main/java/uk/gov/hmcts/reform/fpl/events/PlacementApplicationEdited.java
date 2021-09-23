package uk.gov.hmcts.reform.fpl.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;

@Data
@RequiredArgsConstructor
public class PlacementApplicationEdited {
    private final CaseData caseData;
}
