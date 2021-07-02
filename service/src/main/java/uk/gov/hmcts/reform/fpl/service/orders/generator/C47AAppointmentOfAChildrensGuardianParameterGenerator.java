package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C47ADocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import static uk.gov.hmcts.reform.fpl.model.order.Order.C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C47AAppointmentOfAChildrensGuardianParameterGenerator implements DocmosisParameterGenerator {

    @Override
    public Order accept() {
        return C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        int numberOfChildren = caseData.getAllChildren().size();

        return C47ADocmosisParameters.builder()
            .orderDetails(buildOrderDetails(caseData.getManageOrdersEventData(), numberOfChildren))
            .orderTitle(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN.getTitle())
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }

    private String buildOrderDetails(ManageOrdersEventData manageOrdersEventData, int numberOfChildren) {
        String childGrammar = (numberOfChildren == 1 ? "child" : "children");
        String office = getCafcassOffice(manageOrdersEventData);

        return String.format("The court appoints Cafcass %s as a children's guardian for the %s in the proceedings.",
            office, childGrammar);
    }

    private String getCafcassOffice(ManageOrdersEventData manageOrdersEventData) {
        if ("ENGLAND".equals(manageOrdersEventData.getManageOrdersCafcassRegion())) {
            return manageOrdersEventData.getManageOrdersCafcassOfficesEngland().getLabel();
        }

        return manageOrdersEventData.getManageOrdersCafcassOfficesWales().getLabel();
    }
}
