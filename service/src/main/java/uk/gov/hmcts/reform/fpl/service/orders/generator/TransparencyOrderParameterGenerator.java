package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.TransparencyOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TransparencyOrderParameterGenerator implements DocmosisParameterGenerator {

    private final OrderMessageGenerator orderMessageGenerator;

    @Override
    public Order accept() {
        return Order.TRANSPARENCY_ORDER;
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.TRANSPARENCY_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        return TransparencyOrderDocmosisParameters.builder()
            .orderTitle(Order.TRANSPARENCY_ORDER.getTitle())
            .childrenAct(Order.TRANSPARENCY_ORDER.getChildrenAct())
            .orderByConsent(orderMessageGenerator.getOrderByConsentMessage(eventData))
            .orderExpiration(buildOrderExpiration(caseData))
            .publishInformationDetails(eventData.getManageOrdersTransparencyOrderPublishInformationDetails())
            .publishIdentityDetails(eventData.getManageOrdersTransparencyOrderPublishIdentityDetails())
            .publishDocumentsDetails(eventData.getManageOrdersTransparencyOrderPublishDocumentsDetails())
            .build();
    }

    private String buildOrderExpiration(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        StringBuilder stringBuilder = new StringBuilder();

        switch (eventData.getManageOrdersTransparencyOrderExpiration()) {
            case THE_18TH_BDAY_YOUNGEST_CHILD:
                stringBuilder.append("the 18th birthday of the youngest child.");
                break;
            case DATE_TO_BE_CHOSEN:
                stringBuilder
                    .append(dateBuilder(eventData.getManageOrdersTransparencyOrderEndDate()))
                    .append(".");
                break;
        }

        return stringBuilder.toString();
    }

    private String dateBuilder(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
