package uk.gov.hmcts.reform.fpl.validators;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.Orders;

import java.util.List;

import java.util.stream.Collectors;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class HasEPOGroundsValidatorTest {
    private Validator validator;

    private static final String ERROR_MESSAGE = "Select at least one option for how this case meets grounds for an"
        + " emergency protection order";

    @BeforeEach
    private void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldNotReturnAnErrorIfOrdersAndGroundsForEPODoNotExistInCaseData() {
        CaseData caseData = CaseData.builder().build();

        List<String> errorMessages = validator.validate(caseData).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfOrderTypeIsNotEPO() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EDUCATION_SUPERVISION_ORDER))
                .build())
            .build();

        List<String> errorMessages = validator.validate(caseData).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldNotReturnAnErrorIfOrderTypeEPOExistAndGroundsForEPOReasonExistInCaseData() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .groundsForEPO(GroundsForEPO.builder()
                .reason(ImmutableList.of("Reason"))
                .build())
            .build();

        List<String> errorMessages = validator.validate(caseData).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfOrderTypeEPOExistsInCaseDataButNotGroundsForEPO() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .build();

        List<String> errorMessages = validator.validate(caseData).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnAnErrorIfOrderTypeEPOExistAndGroundsForEPOReasonIsEmptyString() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .groundsForEPO(GroundsForEPO.builder()
                .reason(ImmutableList.of(""))
                .build())
            .build();

        List<String> errorMessages = validator.validate(caseData).stream()
            .map(error -> error.getMessage())
            .collect(Collectors.toList());

        assertThat(errorMessages).contains(ERROR_MESSAGE);
    }
}
