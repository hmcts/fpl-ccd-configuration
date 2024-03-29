package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C44aLeaveToChangeTheSurnameOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import static java.lang.String.format;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C44aLeaveToChangeTheSurnameOrderParameterGenerator implements DocmosisParameterGenerator {

    private final OrderMessageGenerator orderMessageGenerator;

    @Override
    public Order accept() {
        return Order.C44A_LEAVE_TO_CHANGE_A_SURNAME;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        return C44aLeaveToChangeTheSurnameOrderDocmosisParameters.builder()
            .orderTitle(Order.C44A_LEAVE_TO_CHANGE_A_SURNAME.getTitle())
            .orderByConsent(orderMessageGenerator.getOrderByConsentMessage(eventData))
            .orderDetails(orderMessageGenerator.formatOrderMessage(caseData,
                format("The Court grants leave to %s to change the ${childOrChildren} surname to %s",
                    eventData.getManageOrdersPartyGrantedLeave(), eventData.getManageOrdersChildNewSurname())))
            .build();
    }
}
