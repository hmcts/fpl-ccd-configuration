package uk.gov.hmcts.reform.fpl.service.orders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.OrderSectionAndQuestionsPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.preselector.ManageOrderInitialTypePreSelector;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.OrderOperation.AMEND;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrderOperationPostPopulator {
    private final CaseConverter converter;
    private final ManageOrderInitialTypePreSelector manageOrderInitialTypePreSelector;
    private final OrderShowHideQuestionsCalculator showHideQuestionsCalculator;
    private final OrderSectionAndQuestionsPrePopulator sectionAndQuestionsPrePopulator;

    public Map<String, Object> populate(CaseDetails caseDetails) {
        CaseData caseData = converter.convert(caseDetails);
        if (AMEND == caseData.getManageOrdersEventData().getManageOrdersOperation()) {
            Order orderType = Order.AMENED_ORDER;
            Map<String, Object> data = new HashMap<>(Map.of(
                "orderTempQuestions", showHideQuestionsCalculator.calculate(orderType)
            ));
            data.putAll(sectionAndQuestionsPrePopulator.prePopulate(orderType, orderType.firstSection(), caseData));
            return data;
        } else {
            return manageOrderInitialTypePreSelector.preSelect(caseDetails);
        }
    }
}
