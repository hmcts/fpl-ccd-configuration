package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE_TIME;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_EXPIRY_DATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.SUPERVISION_ORDER_END_DATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_CHILDREN;

@ExtendWith(MockitoExtension.class)
class OrderValidatorHolderTest {

    private List<QuestionBlockOrderValidator> validators;

    private Map<OrderQuestionBlock, QuestionBlockOrderValidator> orderQuestionBlockValidators;

    @Mock
    private ApprovalDateValidator approvalDateValidator;
    @Mock
    private ApprovalDateTimeValidator approvalDateTimeValidator;
    @Mock
    private WhichChildrenValidator whichChildrenValidator;
    @Mock
    private EPOEndDateValidator epoEndDateValidator;
    @Mock
    private SupervisionOrderEndDateValidator supervisionOrderEndDateValidator;

    @InjectMocks
    private OrderValidatorHolder underTest;

    @BeforeEach
    void setUp() {
        validators = List.of(
            approvalDateValidator,
            approvalDateTimeValidator,
            whichChildrenValidator,
            epoEndDateValidator,
            supervisionOrderEndDateValidator
        );

        orderQuestionBlockValidators = Map.of(
            APPROVAL_DATE, approvalDateValidator,
            APPROVAL_DATE_TIME, approvalDateTimeValidator,
            WHICH_CHILDREN, whichChildrenValidator,
            EPO_EXPIRY_DATE, epoEndDateValidator,
            SUPERVISION_ORDER_END_DATE, supervisionOrderEndDateValidator
        );

        validators.forEach(validator -> when(validator.accept()).thenCallRealMethod());
    }

    @Test
    void blockToValidator() {
        assertThat(underTest.blockToValidator()).isEqualTo(orderQuestionBlockValidators);
    }

    @Test
    void blockToValidatorCached() {
        underTest.blockToValidator();
        assertThat(underTest.blockToValidator()).isEqualTo(orderQuestionBlockValidators);

        validators.forEach(validator -> verify(validator).accept());
    }
}
