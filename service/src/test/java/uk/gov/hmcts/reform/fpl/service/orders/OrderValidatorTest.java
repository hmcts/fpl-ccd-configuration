package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderValidatorTest {

    private static final Order ORDER = Order.C32_CARE_ORDER;
    private static final OrderSection ORDER_SECTION = OrderSection.SECTION_2;
    private static final CaseDetails CASE_DETAILS = mock(CaseDetails.class);
    private static final OrderQuestionBlock BLOCK_IN_SECTION = OrderQuestionBlock.APPROVER;
    private static final OrderQuestionBlock ANOTHER_BLOCK_IN_SECTION = OrderQuestionBlock.APPROVAL_DATE;
    private static final OrderQuestionBlock BLOCK_NOT_IN_SECTION = OrderQuestionBlock.WHICH_CHILDREN;

    private final QuestionBlockOrderValidator questionBlockOrderValidator = mock(QuestionBlockOrderValidator.class);
    private final QuestionBlockOrderValidator anotherQuestionBlockOrderValidator =
        mock(QuestionBlockOrderValidator.class);
    private final OrderValidatorHolder holder = mock(OrderValidatorHolder.class);

    private OrderValidator underTest = new OrderValidator(holder);

    @Test
    void validateIfEmpty() {

        when(holder.blockToValidator()).thenReturn(Map.of());
        assertThat(underTest.validate(ORDER, ORDER_SECTION, CASE_DETAILS)).isEqualTo(
            List.of()
        );
    }

    @Test
    void validateIfNonMatchingValidator() {
        when(holder.blockToValidator()).thenReturn(Map.of(
            BLOCK_NOT_IN_SECTION, questionBlockOrderValidator)
        );
        when(questionBlockOrderValidator.validate(CASE_DETAILS)).thenReturn(List.of("error1"));

        assertThat(underTest.validate(ORDER, ORDER_SECTION, CASE_DETAILS)).isEqualTo(
            List.of()
        );
    }

    @Test
    void validateIfMatchingValidator() {
        when(holder.blockToValidator()).thenReturn(Map.of(
            BLOCK_IN_SECTION, questionBlockOrderValidator)
        );
        when(questionBlockOrderValidator.validate(CASE_DETAILS)).thenReturn(List.of("error1"));

        assertThat(underTest.validate(ORDER, ORDER_SECTION, CASE_DETAILS)).isEqualTo(
            List.of("error1")
        );
    }

    @Test
    void validateIfMatchingMultipleValidators() {
        Map<OrderQuestionBlock, QuestionBlockOrderValidator> questionMap = new LinkedHashMap<>();
        questionMap.put(BLOCK_IN_SECTION, questionBlockOrderValidator);
        questionMap.put(ANOTHER_BLOCK_IN_SECTION, anotherQuestionBlockOrderValidator);
        when(holder.blockToValidator()).thenReturn(
            questionMap
        );
        when(questionBlockOrderValidator.validate(CASE_DETAILS)).thenReturn(List.of("error1"));
        when(anotherQuestionBlockOrderValidator.validate(CASE_DETAILS)).thenReturn(List.of("error2"));

        assertThat(underTest.validate(ORDER, ORDER_SECTION, CASE_DETAILS)).isEqualTo(
            List.of("error1","error2")
        );
    }

}
