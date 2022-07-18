package uk.gov.hmcts.reform.fpl.service.email.content;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.cafcass.PlacementApplicationCafcassData;
import uk.gov.hmcts.reform.fpl.service.email.content.base.AbstractEmailContentProvider;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlacementEmailContentProvider extends AbstractEmailContentProvider {

    public PlacementApplicationCafcassData buildNewPlacementApplicationNotificationCafcassData(CaseData caseData,
                                                                                          Placement placement) {
        return PlacementApplicationCafcassData.builder()
            .placementChildName(placement.getChildName())
            .build();
    }
}
