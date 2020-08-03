package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.Orders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GroundsValidator.class, LocalValidatorFactoryBean.class})
class GroundsValidatorTest {

    @Autowired
    private GroundsValidator groundsValidator;

    @Test
    void shouldReturnErrorWhenNoGroundsForApplication() {
        final CaseData caseData = CaseData.builder().build();

        final List<String> errors = groundsValidator.validate(caseData);

        assertThat(errors).contains("Add the grounds for the application");
    }

    @Test
    void shouldReturnErrorWhenNoGroundsForApplicationDetails() {
        final Grounds grounds = Grounds.builder().build();

        final CaseData caseData = CaseData.builder()
            .grounds(grounds)
            .build();

        final List<String> errors = groundsValidator.validate(caseData);

        assertThat(errors).containsExactlyInAnyOrder(
            "Select at least one option for how this case meets the threshold criteria",
            "Enter details of how the case meets the threshold criteria"
        );
    }

    @Test
    void shouldReturnEmptyErrorsWhenGroundsForApplicationAreProvided() {
        final Grounds grounds = Grounds.builder()
            .thresholdReason(List.of("Beyond parental control"))
            .thresholdDetails("Custom details")
            .build();

        final CaseData caseData = CaseData.builder()
            .grounds(grounds)
            .build();

        final List<String> errors = groundsValidator.validate(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenEpoOrderRequestedButNoGroundsProvided() {
        final Grounds grounds = Grounds.builder()
            .thresholdReason(List.of("Beyond parental control"))
            .thresholdDetails("Custom details")
            .build();

        final CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .grounds(grounds)
            .build();

        final List<String> errors = groundsValidator.validate(caseData);

        assertThat(errors).containsExactlyInAnyOrder("Add the grounds for the application");
    }

    @Test
    void shouldReturnEmptyErrorsWhenGroundsProvidedForRequestedEpoOrder() {
        final Grounds grounds = Grounds.builder()
            .thresholdReason(List.of("Beyond parental control"))
            .thresholdDetails("Custom details")
            .build();

        final GroundsForEPO groundsForEPO = GroundsForEPO.builder()
            .reason(List.of("Child is likely to suffer harm if they don't stay in their current accommodation"))
            .build();

        final CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(List.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .grounds(grounds)
            .groundsForEPO(groundsForEPO)
            .build();

        final List<String> errors = groundsValidator.validate(caseData);

        assertThat(errors).isEmpty();
    }
}
