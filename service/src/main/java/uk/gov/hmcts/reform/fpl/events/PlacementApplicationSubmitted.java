package uk.gov.hmcts.reform.fpl.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;

@Data
@RequiredArgsConstructor
public class PlacementApplicationSubmitted {

    private final CaseData caseData;
    private final Placement placement;
}
