package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderOperation;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.OrderSectionAndQuestionsPrePopulator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.preselector.ManageOrderInitialTypePreSelector;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.order.Order.AMENED_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderOperation.AMEND;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.AMEND_DOWNLOAD;

class ManageOrderOperationPostPopulatorTest {
    private final CaseDetails caseDetails = mock(CaseDetails.class);
    private final CaseData caseData = mock(CaseData.class);
    private final ManageOrdersEventData eventData = mock(ManageOrdersEventData.class);

    private final CaseConverter converter = mock(CaseConverter.class);
    private final ManageOrderInitialTypePreSelector preSelector = mock(ManageOrderInitialTypePreSelector.class);
    private final OrderShowHideQuestionsCalculator calculator = mock(OrderShowHideQuestionsCalculator.class);
    private final OrderSectionAndQuestionsPrePopulator prePopulator = mock(OrderSectionAndQuestionsPrePopulator.class);

    private final ManageOrderOperationPostPopulator underTest = new ManageOrderOperationPostPopulator(
        converter, preSelector, calculator, prePopulator
    );

    @BeforeEach
    void setUp() {
        when(converter.convert(caseDetails)).thenReturn(caseData);
        when(caseData.getManageOrdersEventData()).thenReturn(eventData);
    }

    @Test
    void populateAmendOrderOperation() {
        Map<String, String> showHideFields = Map.of("fields from", "the calculator");
        Map<String, Object> prePopulatedFields = Map.of("pre-populated", "fields");

        when(eventData.getManageOrdersOperation()).thenReturn(AMEND);
        when(calculator.calculate(AMENED_ORDER)).thenReturn(showHideFields);
        when(prePopulator.prePopulate(AMENED_ORDER, AMEND_DOWNLOAD, caseData)).thenReturn(prePopulatedFields);

        Map<String, Object> expectedMap = new HashMap<>(Map.of("orderTempQuestions", showHideFields));
        expectedMap.putAll(prePopulatedFields);

        assertThat(underTest.populate(caseDetails)).isEqualTo(expectedMap);
    }

    @Test
    void populateAmendOrderOperationClosed() {
        Map<String, String> showHideFields = Map.of("fields from", "the calculator");
        Map<String, Object> prePopulatedFields = Map.of("pre-populated", "fields");

        when(eventData.getManageOrdersOperation()).thenReturn(null);
        when(eventData.getManageOrdersOperationClosedState()).thenReturn(AMEND);
        when(calculator.calculate(AMENED_ORDER)).thenReturn(showHideFields);
        when(prePopulator.prePopulate(AMENED_ORDER, AMEND_DOWNLOAD, caseData)).thenReturn(prePopulatedFields);

        Map<String, Object> expectedMap = new HashMap<>(Map.of("orderTempQuestions", showHideFields));
        expectedMap.putAll(prePopulatedFields);

        assertThat(underTest.populate(caseDetails)).isEqualTo(expectedMap);
    }

    @Test
    void populateNotAmendOrderOperation() {
        Map<String, Object> preSelectedFields = Map.of("pre-selected", "fields");

        when(eventData.getManageOrdersOperation()).thenReturn(OrderOperation.CREATE);
        when(preSelector.preSelect(caseDetails)).thenReturn(preSelectedFields);

        assertThat(underTest.populate(caseDetails)).isEqualTo(preSelectedFields);
    }
}
