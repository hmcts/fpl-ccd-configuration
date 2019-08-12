package uk.gov.hmcts.reform.fpl.Validators;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.validators.HasThresholdCriteriaDetailsValidator;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class HasThresholdCriteriaDetailsValidatorTest {
    private HasThresholdCriteriaDetailsValidator validator = new HasThresholdCriteriaDetailsValidator();
    private ConstraintValidatorContext constraintValidatorContext;

    @Test
    void shouldReturnFalseIfGetOrdersDoesNotExistOnCaseData() {
        CaseData caseData = CaseData.builder().build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseWhenOrderTypeContainsCareOrderOnly() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.CARE_ORDER))
                .build())
            .build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);

        assertThat(isValid).isFalse();
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
