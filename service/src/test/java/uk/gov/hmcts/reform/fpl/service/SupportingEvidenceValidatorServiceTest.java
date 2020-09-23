package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SupportingEvidenceValidatorService.class, ValidateGroupService.class,
    LocalValidatorFactoryBean.class, FixedTimeConfiguration.class})
class SupportingEvidenceValidatorServiceTest {

    private static final String ERROR_MESSAGE = "Date received cannot be in the future";

    @Autowired
    private Time time;

    @Autowired
    private SupportingEvidenceValidatorService supportingEvidenceValidatorService;

    @Test
    void shouldNotReturnValidationErrorIfSupportingEvidenceBundleDateAndTimeReceivedIsInThePast() {
        LocalDateTime yesterday = time.now().minusDays(1);
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle = buildSupportingEvidenceBundle(yesterday);
        List<String> validationErrors = supportingEvidenceValidatorService.validate(supportingEvidenceBundle);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldNotReturnValidationErrorIfSupportingEvidenceBundleDateAndTimeReceivedIsInThePresent() {
        LocalDateTime today = time.now();
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle = buildSupportingEvidenceBundle(today);
        List<String> validationErrors = supportingEvidenceValidatorService.validate(supportingEvidenceBundle);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnValidationErrorIfSupportingEvidenceBundleDateAndTimeReceivedIsInTheFuture() {
        LocalDateTime futureDate = time.now().plusDays(2);
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle = buildSupportingEvidenceBundle(futureDate);
        List<String> validationErrors = supportingEvidenceValidatorService.validate(supportingEvidenceBundle);

        assertThat(validationErrors).containsExactly(ERROR_MESSAGE);
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle(LocalDateTime dateTimeReceived) {
        return List.of(
            element(SupportingEvidenceBundle.builder()
                .dateTimeReceived(dateTimeReceived)
                .build()));
    }
}
