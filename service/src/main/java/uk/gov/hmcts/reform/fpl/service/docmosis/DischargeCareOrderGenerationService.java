package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DischargeCareOrderService;

import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.OrderHelper.getFullOrderType;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DischargeCareOrderGenerationService extends GeneratedOrderTemplateDataGeneration {

    private final HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;
    private final DischargeCareOrderService dischargeCareOrder;

    @SuppressWarnings("rawtypes")
    @Override
    DocmosisGeneratedOrderBuilder populateCustomOrderFields(CaseData caseData) {
        OrderTypeAndDocument orderTypeAndDocument = caseData.getOrderTypeAndDocument();

        return DocmosisGeneratedOrder.builder()
            .orderTitle(getFullOrderType(orderTypeAndDocument))
            .childrenAct("Section 39(1) Children Act 1989")
            .orderDetails(formatOrderDetails(caseData));
    }

    @Override
    List<Element<Child>> getSelectedChildren(CaseData caseData) {
        return wrapElements(dischargeCareOrder.getChildrenInSelectedCareOrders(caseData));
    }

    private String formatOrderDetails(CaseData caseData) {
        final List<GeneratedOrder> careOrders = dischargeCareOrder.getSelectedCareOrders(caseData);

        final String defaultCourtName = getDefaultCourt(caseData);

        if (careOrders.size() == 1) {
            GeneratedOrder careOrder = careOrders.get(0);
            return format("The court discharges the care order made by the %s on %s",
                defaultIfNull(careOrder.getCourtName(), defaultCourtName),
                careOrder.getDateOfIssue());
        } else {
            String formatted = careOrders.stream()
                .map(order -> format("The care order made by the %s on %s",
                    defaultIfNull(order.getCourtName(), defaultCourtName),
                    order.getDateOfIssue()))
                .collect(joining("\n"));

            return format("The court discharges:%n%s", formatted);
        }
    }

    private String getDefaultCourt(CaseData caseData) {
        return hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName();
    }
}
