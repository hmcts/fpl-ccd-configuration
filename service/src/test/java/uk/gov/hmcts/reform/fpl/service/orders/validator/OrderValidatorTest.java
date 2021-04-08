package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrderValidatorTest {

    private static final Order ORDER = Order.C32_CARE_ORDER;
    private static final OrderSection ORDER_SECTION = OrderSection.SECTION_2;
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final OrderQuestionBlock BLOCK_IN_SECTION = OrderQuestionBlock.APPROVER;
    private static final OrderQuestionBlock ANOTHER_BLOCK_IN_SECTION = OrderQuestionBlock.APPROVAL_DATE;
    private static final OrderQuestionBlock BLOCK_NOT_IN_SECTION = OrderQuestionBlock.WHICH_CHILDREN;

    private final QuestionBlockOrderValidator questionBlockValidator = mock(QuestionBlockOrderValidator.class);
    private final QuestionBlockOrderValidator anotherQuestionBlockValidator = mock(QuestionBlockOrderValidator.class);
    private final OrderValidatorHolder holder = mock(OrderValidatorHolder.class);

    private OrderValidator underTest = new OrderValidator(holder);

    @Test
    void validateIfEmpty() {
        when(holder.blockToValidator()).thenReturn(Map.of());

        assertThat(underTest.validate(ORDER, ORDER_SECTION, CASE_DATA)).isEqualTo(
            List.of()
        );
    }

    @Test
    void validateIfNonMatchingValidator() {
        when(holder.blockToValidator()).thenReturn(Map.of(
            BLOCK_NOT_IN_SECTION, questionBlockValidator
        ));

        assertThat(underTest.validate(ORDER, ORDER_SECTION, CASE_DATA)).isEqualTo(
            List.of()
        );

        verifyNoInteractions(questionBlockValidator);
    }

    @Test
    void validateIfMatchingValidator() {
        when(holder.blockToValidator()).thenReturn(Map.of(
            BLOCK_IN_SECTION, questionBlockValidator
        ));

        when(questionBlockValidator.validate(CASE_DATA)).thenReturn(List.of("error1"));

        assertThat(underTest.validate(ORDER, ORDER_SECTION, CASE_DATA)).isEqualTo(
            List.of("error1")
        );
    }

    @Test
    void validateIfMatchingMultipleValidators() {
        when(holder.blockToValidator()).thenReturn(Map.of(
            BLOCK_IN_SECTION, questionBlockValidator,
            ANOTHER_BLOCK_IN_SECTION, anotherQuestionBlockValidator
        ));

        when(questionBlockValidator.validate(CASE_DATA)).thenReturn(List.of("error1"));
        when(anotherQuestionBlockValidator.validate(CASE_DATA)).thenReturn(List.of("error2"));

        assertThat(underTest.validate(ORDER, ORDER_SECTION, CASE_DATA)).isEqualTo(
            List.of("error1", "error2")
        );
    }
}
