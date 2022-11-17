package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C63aDeclarationOfParentageDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C63aDeclarationOfParentageDocumentParameterGenerator implements DocmosisParameterGenerator {

    private final ChildrenSmartSelector childrenSmartSelector;

    @Override
    public Order accept() {
        return Order.C63A_DECLARATION_OF_PARENTAGE;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        return C63aDeclarationOfParentageDocmosisParameters.builder()
            .orderTitle(Order.C63A_DECLARATION_OF_PARENTAGE.getTitle())
            .childrenAct(Order.C63A_DECLARATION_OF_PARENTAGE.getChildrenAct())
            .orderMessage(buildOrderMessage(eventData))
            .orderDetails(buildOrderDetails(caseData, eventData))
            .build();
    }

    private String buildOrderDetails(CaseData caseData, ManageOrdersEventData eventData) {
        String ret = "It is declared that "
            + eventData.getManageOrdersPersonWhoseParenthoodIs().getValue().getCode() + " ";
        ret += eventData.getManageOrdersParentageAction().getValue().getLabel() + " the parent of ";
        ret += childrenSmartSelector.getSelectedChildren(caseData).get(0).getValue().getParty().getFullName();
        return ret + ".";
    }

    private String buildOrderMessage(ManageOrdersEventData eventData) {
        String ret = "Upon the application of "
            + eventData.getManageOrdersParentageApplicant().getValue().getLabel();
        if (eventData.getManageOrdersHearingParty1() != null) {
            ret += "\nand upon hearing " + eventData.getManageOrdersHearingParty1().getValue().getCode();
        }
        if (eventData.getManageOrdersHearingParty2() != null) {
            ret += "\nand upon hearing " + eventData.getManageOrdersHearingParty2().getValue().getCode();
        }
        return ret + ".";
    }

}
