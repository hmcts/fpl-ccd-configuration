package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.preselector;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.orders.OrderShowHideQuestionsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.OrderSectionAndQuestionsPrePopulator;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageOrderInitialTypePreSelector {

    private final CaseConverter caseConverter;
    private final OrderSectionAndQuestionsPrePopulator orderSectionAndQuestionsPrePopulator;
    private final OrderShowHideQuestionsCalculator showHideQuestionsCalculator;

    public Map<String, Object> preSelect(CaseDetails caseDetails) {
        CaseData caseData = caseConverter.convert(caseDetails);

        if (caseData.getState() == State.CLOSED) {
            Map<String, Object> defaultClosedStateData = new HashMap<>();
            defaultClosedStateData.put("manageOrdersState", State.CLOSED);
            defaultClosedStateData.put("manageOrdersType", C21_BLANK_ORDER);
            defaultClosedStateData.put("orderTempQuestions", showHideQuestionsCalculator.calculate(C21_BLANK_ORDER));

            defaultClosedStateData.putAll(orderSectionAndQuestionsPrePopulator.prePopulate(
                C21_BLANK_ORDER,
                C21_BLANK_ORDER.firstSection(),
                caseDataWithDefaults(caseDetails, defaultClosedStateData))
            );

            return defaultClosedStateData;
        }

        return Map.of();
    }

    private CaseData caseDataWithDefaults(CaseDetails caseDetails, Map<String, Object> defaultClosedDate) {
        CaseDetails caseDetailsWithDefaults = caseDetails.toBuilder().build();
        caseDetailsWithDefaults.getData().putAll(defaultClosedDate);
        CaseData convert = caseConverter.convert(caseDetailsWithDefaults);
        return convert;
    }

}
