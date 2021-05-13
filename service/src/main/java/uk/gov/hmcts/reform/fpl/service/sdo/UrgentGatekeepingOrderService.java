package uk.gov.hmcts.reform.fpl.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.service.CourtLevelAllocationService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UrgentGatekeepingOrderService {

    private final CourtLevelAllocationService allocationService;
    private final DocumentSealingService sealingService;
    private final Time time;

    public GatekeepingOrderEventData prePopulate(CaseData caseData) {
        GatekeepingOrderEventData.GatekeepingOrderEventDataBuilder eventData = GatekeepingOrderEventData.builder();

        if (noPreExistingAllocationDecision(caseData)) {
            eventData.showUrgentHearingAllocation(YES);
            eventData.urgentHearingAllocation(allocationService.createDecision(caseData));
        } else {
            eventData.showUrgentHearingAllocation(NO);
        }

        return eventData.build();
    }

    public Map<String, Object> finalise(CaseData caseData) {
        final GatekeepingOrderEventData eventData = caseData.getGatekeepingOrderEventData();
        final DocumentReference orderDocument = eventData.getUrgentHearingOrderDocument();
        final Map<String, Object> returnedData = new HashMap<>();

        Allocation allocationDecision = null;

        if (noPreExistingAllocationDecision(caseData)) {
            allocationDecision = allocationService.setAllocationDecisionIfNull(
                caseData, eventData.getUrgentHearingAllocation()
            );

            returnedData.put("allocationDecision", allocationDecision);
        }

        final UrgentHearingOrder order = UrgentHearingOrder.builder()
            .order(sealingService.sealDocument(orderDocument))
            .unsealedOrder(orderDocument)
            .dateAdded(time.now().toLocalDate())
            .allocation(allocationDecision)
            .build();

        returnedData.put("urgentHearingOrder", order);

        return returnedData;
    }

    private boolean noPreExistingAllocationDecision(CaseData caseData) {
        return null == caseData.getAllocationDecision();
    }
}
