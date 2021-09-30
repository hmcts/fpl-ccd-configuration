package uk.gov.hmcts.reform.fpl.events;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class PlacementNoticeChanged {

    private final CaseData caseData;
    private final Placement placement;
    private final PlacementNoticeDocument notice;
}
