package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.A206PlacementOrderNotificationDocmosisParameters;

import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.A206;
import static uk.gov.hmcts.reform.fpl.model.order.Order.A70_PLACEMENT_ORDER;

@Component
@RequiredArgsConstructor
public class A206PlacementOrderNotificationParameterGenerator implements DocmosisParameterGenerator {

    private final PlacementService placementService;

    @Override
    public Order accept() {
        return A70_PLACEMENT_ORDER;
    }

    @Override
    public A206PlacementOrderNotificationDocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData manageOrdersEventData = caseData.getManageOrdersEventData();
        UUID placementId = manageOrdersEventData.getManageOrdersChildPlacementApplication().getValueCodeAsUUID();
        Element<Child> child = placementService.getChildByPlacementId(caseData, placementId);
        ChildParty childInfo = child.getValue().getParty();

        return A206PlacementOrderNotificationDocmosisParameters.builder()
            .serialNumber(manageOrdersEventData.getManageOrdersSerialNumber())
            .childrenAct("Adoption and Children Act 2002")
            .child(DocmosisChild.builder().name(childInfo.getFullName()).build())
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return A206;
    }

}
