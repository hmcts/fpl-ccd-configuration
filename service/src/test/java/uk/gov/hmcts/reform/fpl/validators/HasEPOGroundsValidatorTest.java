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
class HasEPOGroundsValidatorTest {
    private HasEPOGroundsValidator validator = new HasEPOGroundsValidator();

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
    void shouldReturnTrueIfOrdersAndGroundsForEPODoNotExistInCaseData() {
        CaseData caseData = CaseData.builder().build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnTrueIfOrderTypeIsNotEPO() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EDUCATION_SUPERVISION_ORDER))
                .build())
            .build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseIfOrderTypeEPOExistsInCaseDataButNotGroundsForEPO() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnTrueIfOrderTypeEPOExistAndGroundsForEPOReasonExistInCaseData() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .groundsForEPO(GroundsForEPO.builder()
                .reason(ImmutableList.of("Reason"))
                .build())
            .build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseIfOrderTypeEPOExistAndGroundsForEPOReasonIsEmptyString() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .groundsForEPO(GroundsForEPO.builder()
                .reason(ImmutableList.of(""))
                .build())
            .build();

        Boolean isValid = validator.isValid(caseData, constraintValidatorContext);

        assertThat(isValid).isFalse();
    }
}
