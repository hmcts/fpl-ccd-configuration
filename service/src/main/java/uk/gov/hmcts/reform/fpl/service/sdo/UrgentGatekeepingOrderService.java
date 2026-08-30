package uk.gov.hmcts.reform.fpl.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.GatekeepingOrderEventData;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.service.CourtLevelAllocationService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UrgentGatekeepingOrderService {

    private static final String URGENT_HEARING_ORDER = "urgentHearingOrder";
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

        String allocation = null;

        if (noPreExistingAllocationDecision(caseData)) {
            Allocation allocationDecision = allocationService.createAllocationDecisionIfNull(
                caseData, eventData.getUrgentHearingAllocation()
            );

            allocation = allocationDecision.getProposalV2();

            returnedData.put("allocationDecision", allocationDecision);
        }

        final UrgentHearingOrder order = UrgentHearingOrder.builder()
            .order(orderDocument)
            .unsealedOrder(orderDocument)
            .dateAdded(time.now().toLocalDate())
            .translationRequirements(eventData.getUrgentGatekeepingTranslationRequirements())
            .allocation(allocation)
            .build();

        returnedData.put(URGENT_HEARING_ORDER, order);

        return returnedData;
    }

    public Map<String, Object> finaliseAndSeal(CaseData caseData) {
        final GatekeepingOrderEventData eventData = caseData.getGatekeepingOrderEventData();
        final DocumentReference orderDocument = eventData.getUrgentHearingOrderDocument();
        final Map<String, Object> returnedData = new HashMap<>();

        String allocation = null;

        if (noPreExistingAllocationDecision(caseData)) {
            Allocation allocationDecision = allocationService.createAllocationDecisionIfNull(
                caseData, eventData.getUrgentHearingAllocation()
            );

            allocation = allocationDecision.getProposalV2();

            returnedData.put("allocationDecision", allocationDecision);
        }

        final UrgentHearingOrder order = UrgentHearingOrder.builder()
            .order(sealingService.sealDocument(orderDocument, caseData.getCourt(), caseData.getSealType()))
            .unsealedOrder(orderDocument)
            .dateAdded(time.now().toLocalDate())
            .translationRequirements(eventData.getUrgentGatekeepingTranslationRequirements())
            .allocation(allocation)
            .build();

        returnedData.put(URGENT_HEARING_ORDER, order);

        return returnedData;
    }

    @Deprecated
    public List<DocmosisTemplates> getNoticeOfProceedingsTemplates(CaseData caseData) {
        List<DocmosisTemplates> templates = new ArrayList<>();
        templates.add(DocmosisTemplates.C6);

        if (!caseData.getOthersV2().isEmpty()) {
            templates.add(DocmosisTemplates.C6A);
        }

        return templates;
    }

    public Map<String, Object> sealDocumentAfterEventSubmitted(CaseData caseData) {
        Map<String, Object> updates = new HashMap<>();
        final UrgentHearingOrder urgentHearingOrder = caseData.getUrgentHearingOrder();
        final UrgentHearingOrder sealedOrder = UrgentHearingOrder.builder()
            .order(sealingService.sealDocument(urgentHearingOrder.getUnsealedOrder(),
                caseData.getCourt(), caseData.getSealType()))
            .unsealedOrder(urgentHearingOrder.getUnsealedOrder())
            .dateAdded(urgentHearingOrder.getDateAdded())
            .translationRequirements(urgentHearingOrder.getTranslationRequirements())
            .allocation(urgentHearingOrder.getAllocation())
            .build();

        updates.put(URGENT_HEARING_ORDER, sealedOrder);
        return updates;
    }

    private boolean noPreExistingAllocationDecision(CaseData caseData) {
        return null == caseData.getAllocationDecision();
    }

}
