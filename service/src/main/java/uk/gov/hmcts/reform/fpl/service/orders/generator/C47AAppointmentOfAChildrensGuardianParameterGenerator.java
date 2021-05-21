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

        return C47ADocmosisParameters.builder()
            .orderDetails(buildOrderDetails(caseData.getManageOrdersEventData()))
            .orderTitle(C47A_APPOINTMENT_OF_A_CHILDRENS_GUARDIAN.getTitle())
            .furtherDirections(eventData.getManageOrdersFurtherDirections())
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER;
    }

    private String buildOrderDetails(ManageOrdersEventData manageOrdersEventData) {
        String region = getJurisdictionRegion(manageOrdersEventData);

        return String.format("The court appoints Cafcass %s as a Children's Guardian for the child in the"
            + " proceedings.", region);
    }

    private String getJurisdictionRegion(ManageOrdersEventData manageOrdersEventData) {
        if ("ENGLAND".equals(manageOrdersEventData.getManageOrdersJurisdictionRegion())) {
            return manageOrdersEventData.getManageOrdersEnglandRegions().getLabel();
        }

        return manageOrdersEventData.getManageOrdersWalesRegions().getLabel();
    }
}
