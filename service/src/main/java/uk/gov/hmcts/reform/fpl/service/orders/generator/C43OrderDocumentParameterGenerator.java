package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.C43OrderType;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C43DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class C43OrderDocumentParameterGenerator implements DocmosisParameterGenerator {

    private final LocalAuthorityNameLookupConfiguration laNameLookup;


    @Override
    public Order accept() {
        return Order.C43_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();

        String localAuthorityCode = caseData.getCaseLocalAuthority();
        String localAuthorityName = laNameLookup.getLocalAuthorityName(localAuthorityCode);

        return C43DocmosisParameters.builder()
            .orderTitle(getOrderTitle(eventData))
            .orderDetails(getOrderRecitalsAndPreambles(eventData))
            .furtherDirections(getOrderDirections(eventData))
            .localAuthorityName(localAuthorityName)
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return DocmosisTemplates.ORDER;
    }

    private String getOrderTitle(ManageOrdersEventData eventData) {
        List<C43OrderType> orders = eventData.getManageOrdersC43Orders();

        return orders.stream().map(
            C43OrderType::getLabel
        ).collect(Collectors.joining(" and ")) + " order";
    }

    private String getOrderRecitalsAndPreambles(ManageOrdersEventData eventData) {
        String recitals = eventData.getManageOrdersC43RecitalsAndPreambles();

        String orderDetails = "The Court orders";

        if (!isEmpty(recitals)) {
            orderDetails += "\n\n " + recitals;
        }

        return orderDetails;
    }

    private String getOrderDirections(ManageOrdersEventData eventData) {
        String directions = eventData.getManageOrdersC43Directions();
        String furtherDirections = eventData.getManageOrdersFurtherDirections();

        if (!isEmpty(furtherDirections)) {
            return directions + "\n\n " + furtherDirections;
        }

        return directions;
    }
}
