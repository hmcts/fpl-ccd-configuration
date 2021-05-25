package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.preselector;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.orders.OrderShowHideQuestionsCalculator;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.OrderSectionAndQuestionsPrePopulator;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.State.CLOSED;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C21_BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.model.order.OrderSection.ISSUING_DETAILS;

class ManageOrderInitialTypePreSelectorTest {

    private static final CaseDetails CASE_DETAILS = CaseDetails.builder().data(new HashMap<>()).build();
    private static final Map<String, String> SHOW_HIDE = Map.of("showHide", "value");
    private static final CaseData MODIFIED_CASE_DATA = mock(CaseData.class);

    private final OrderShowHideQuestionsCalculator showHideQuestionsCalculator =
        mock(OrderShowHideQuestionsCalculator.class);
    private final OrderSectionAndQuestionsPrePopulator orderSectionAndQuestionsPrePopulator = mock(
        OrderSectionAndQuestionsPrePopulator.class);
    private final CaseConverter caseConverter = mock(CaseConverter.class);

    private final ManageOrderInitialTypePreSelector underTest = new ManageOrderInitialTypePreSelector(
        caseConverter, orderSectionAndQuestionsPrePopulator,
        showHideQuestionsCalculator
    );

    @Test
    void testWhenNotClosedState() {
        when(caseConverter.convert(CASE_DETAILS)).thenReturn(CaseData.builder().build());

        Map<String, Object> actual = underTest.preSelect(CASE_DETAILS);

        assertThat(actual).isEqualTo(Map.of());
    }

    @Test
    void testWhenClosedState() {
        when(caseConverter.convert(CASE_DETAILS)).thenReturn(CaseData.builder().state(CLOSED).build());
        when(showHideQuestionsCalculator.calculate(C21_BLANK_ORDER)).thenReturn(SHOW_HIDE);
        when(caseConverter.convert(CaseDetails.builder().data(
            Map.of("manageOrdersState", CLOSED,
                "manageOrdersType", C21_BLANK_ORDER,
                "orderTempQuestions", SHOW_HIDE
            )
        ).build())).thenReturn(MODIFIED_CASE_DATA);
        when(orderSectionAndQuestionsPrePopulator.prePopulate(C21_BLANK_ORDER, ISSUING_DETAILS, MODIFIED_CASE_DATA))
            .thenReturn(Map.of("sectionAndQuestions", "value"));

        Map<String, Object> actual = underTest.preSelect(CASE_DETAILS);

        assertThat(actual).isEqualTo(Map.of(
            "manageOrdersState", CLOSED,
            "manageOrdersType", C21_BLANK_ORDER,
            "orderTempQuestions", SHOW_HIDE,
            "sectionAndQuestions", "value"
        ));
    }
}
