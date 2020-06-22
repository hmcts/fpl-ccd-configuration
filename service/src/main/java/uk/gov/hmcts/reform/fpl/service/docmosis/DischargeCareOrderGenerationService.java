package uk.gov.hmcts.reform.fpl.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.OrderTypeAndDocument;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisDischargeOfCareOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisGeneratedOrder.DocmosisGeneratedOrderBuilder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DischargeCareOrderService;

import java.util.List;
import java.util.stream.Collectors;

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

        return DocmosisDischargeOfCareOrder.builder()
            .orderTitle(getFullOrderType(orderTypeAndDocument))
            .childrenAct("Section 39(1) Children Act 1989")
            .careOrders(extractCareOrders(caseData));
    }

    @Override
    List<Element<Child>> getSelectedChildren(CaseData caseData) {
        return wrapElements(dischargeCareOrder.getChildrenInSelectedCareOrders(caseData));
    }

    private List<DocmosisOrder> extractCareOrders(CaseData caseData) {
        final List<GeneratedOrder> careOrders = dischargeCareOrder.getSelectedCareOrders(caseData);
        final String defaultCourtName = getDefaultCourt(caseData);

        return careOrders.stream()
            .map(order -> DocmosisOrder.builder()
                .courtName(defaultIfNull(order.getCourtName(), defaultCourtName))
                .dateOfIssue(order.getDateOfIssue())
                .build())
            .collect(Collectors.toList());
    }

    private String getDefaultCourt(CaseData caseData) {
        return hmctsCourtLookupConfiguration.getCourt(caseData.getCaseLocalAuthority()).getName();
    }
}
