package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.RespondentsRefusedFormatter;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C34BAuthorityToRefuseContactDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C34BAuthorityToRefuseContactOrderParameterGenerator implements DocmosisParameterGenerator {

    private final OrderMessageGenerator orderMessageGenerator;
    private final RespondentsRefusedFormatter respondentsRefusedFormatter;
    @Override
    public Order accept() {
        return Order.C34B_AUTHORITY_TO_REFUSE_CONTACT;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        return C34BAuthorityToRefuseContactDocmosisParameters.builder()
            .orderTitle(Order.C34B_AUTHORITY_TO_REFUSE_CONTACT.getTitle())
            .childrenAct(Order.C34B_AUTHORITY_TO_REFUSE_CONTACT.getChildrenAct())
            .orderMessage(orderMessageGenerator.formatOrderMessage(caseData,"The local authority is ${localAuthorityName}"))
            .orderByConsent(orderMessageGenerator.getOrderByConsentMessage(eventData))
            .orderDetails(getRespondentsRefusedMessage(caseData))
            .build();
    }

    String getRespondentsRefusedMessage(CaseData caseData) {
        String applicant = respondentsRefusedFormatter.getRespondentsNamesForDocument(caseData);

        return orderMessageGenerator.formatOrderMessage(caseData,"The Court orders that the local authority is authorised to refuse contact between the ${childOrChildren} and "
            ) + applicant;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER_V2;
    }
}
