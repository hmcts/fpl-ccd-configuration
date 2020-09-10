package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    ValidateSupportingEvidenceBundleService.class,
    ValidateGroupService.class,
    LocalValidatorFactoryBean.class
})
class ValidateSupportingEvidenceBundleServiceTest {

    private static final String ERROR_MESSAGE = "Date of time received cannot be in the future";

    @Autowired
    private ValidateSupportingEvidenceBundleService validate;

    private SupportingEvidenceBundle supportingEvidenceBundle;

    @BeforeEach
    void setUp() {
        supportingEvidenceBundle = SupportingEvidenceBundle.builder().build();
    }

    @Test
    void shouldReturnErrorsWhenTheDateOfIssueIsInFuture() {
        SupportingEvidenceBundle updated = supportingEvidenceBundle.toBuilder()
            .dateTimeReceived(LocalDateTime.now().plusDays(1))
            .build();

        assertThat(validate.validateBundle(Collections.singletonList(updated)).toArray()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoErrorsWhenTheDateOfIssueIsNow() {
        SupportingEvidenceBundle updated = SupportingEvidenceBundle.builder()
            .dateTimeReceived(LocalDateTime.now())
            .build();

        assertThat(validate.validateBundle(Collections.singletonList(updated)).size()).isZero();
    }

    @Test
    void shouldReturnNoErrorsWhenTheDateOfIssueIsInPast() {
        SupportingEvidenceBundle updated = supportingEvidenceBundle.toBuilder()
            .dateTimeReceived(LocalDateTime.now().minusDays(1))
            .build();

        assertThat(validate.validateBundle(Collections.singletonList(updated)).size()).isZero();
    }
}
