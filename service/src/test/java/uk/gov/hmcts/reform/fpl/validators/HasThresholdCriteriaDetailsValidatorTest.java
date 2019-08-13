package uk.gov.hmcts.reform.fpl.validators;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.Orders;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class HasThresholdCriteriaDetailsValidatorTest {
    private HasThresholdCriteriaDetailsValidator validator = new HasThresholdCriteriaDetailsValidator();

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext
        nodeBuilderCustomizableContext;

    @BeforeEach
    private void prepareMocks() {
        given(constraintValidatorContext.getDefaultConstraintMessageTemplate()).willReturn("");
        given(constraintValidatorContext.buildConstraintViolationWithTemplate(Mockito.anyString()))
            .willReturn(constraintViolationBuilder);
        given(constraintViolationBuilder.addPropertyNode(Mockito.anyString()))
            .willReturn(nodeBuilderCustomizableContext);
    }

    @Test
    void shouldReturnTrueIfGetOrdersDoesNotExistOnCaseData() {
        CaseData caseData = CaseData.builder().build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnTrueWhenOrderTypeContainsCareOrderOnly() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.CARE_ORDER))
                .build())
            .build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOrderTypeDoesContainEPOButNotThresholdDetails() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnTrueWhenOrderTypeDoesContainEPOAndThresholdDetails() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .groundsForEPO(GroundsForEPO.builder()
                .thresholdDetails("details")
                .build())
            .build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseWhenOrderTypeDoesContainEPOAndThresholdDetailsIsEmpty() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .groundsForEPO(GroundsForEPO.builder()
                .thresholdDetails("")
                .build())
            .build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);

        assertThat(isValid).isFalse();
    }
}
