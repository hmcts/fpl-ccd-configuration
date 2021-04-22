package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.APPROVAL_DATE_TIME;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_EXPIRY_DATE;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_PREVENT_REMOVAL;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.WHICH_CHILDREN;

@ExtendWith(MockitoExtension.class)
class OrderValidatorHolderTest {

    private List<QuestionBlockOrderValidator> validators;

    @Mock
    private ApprovalDateValidator approvalDateValidator;
    @Mock
    private ApprovalDateTimeValidator approvalDateTimeValidator;
    @Mock
    private WhichChildrenValidator whichChildrenValidator;
    @Mock
    private EPORemovalAddressValidator epoRemovalAddressValidator;
    @Mock
    private EPOEndDateValidator epoEndDateValidator;

    @InjectMocks
    private OrderValidatorHolder underTest;

    @BeforeEach
    void setUp() {
        validators = List.of(
            approvalDateValidator, approvalDateTimeValidator, whichChildrenValidator,
            epoRemovalAddressValidator, epoEndDateValidator);

        validators.forEach(validator -> when(validator.accept()).thenCallRealMethod());
    }

    @Test
    void blockToValidator() {
        assertThat(underTest.blockToValidator()).isEqualTo(Map.of(
            APPROVAL_DATE, approvalDateValidator,
            APPROVAL_DATE_TIME, approvalDateTimeValidator,
            WHICH_CHILDREN, whichChildrenValidator,
            EPO_PREVENT_REMOVAL, epoRemovalAddressValidator,
            EPO_EXPIRY_DATE, epoEndDateValidator
        ));
    }

    @Test
    void blockToValidatorCached() {
        underTest.blockToValidator();
        assertThat(underTest.blockToValidator()).isEqualTo(Map.of(
            APPROVAL_DATE, approvalDateValidator,
            APPROVAL_DATE_TIME, approvalDateTimeValidator,
            WHICH_CHILDREN, whichChildrenValidator,
            EPO_PREVENT_REMOVAL, epoRemovalAddressValidator,
            EPO_EXPIRY_DATE, epoEndDateValidator
        ));

        validators.forEach(validator -> verify(validator).accept());
    }
}
