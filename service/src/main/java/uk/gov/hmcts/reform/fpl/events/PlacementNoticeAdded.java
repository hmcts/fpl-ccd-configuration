package uk.gov.hmcts.reform.fpl.events;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class PlacementNoticeAdded {

    private final CaseData caseData;
    private final Placement placement;
}
