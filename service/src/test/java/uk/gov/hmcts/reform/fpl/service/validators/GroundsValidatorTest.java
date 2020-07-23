package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Grounds;

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

        assertThat(errors).contains("You need to add details to grounds for the application");
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
}
