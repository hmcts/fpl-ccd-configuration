package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.validation.groups.DateOfIssueGroup;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SupportingEvidenceValidatorService.class, ValidateGroupService.class,
    LocalValidatorFactoryBean.class, FixedTimeConfiguration.class})
public class SupportingEvidenceValidatorServiceTest {

    private static final String VALIDATION_ERROR = "Error 1";

    @MockBean
    private ValidateGroupService validateGroupService;

    @Autowired
    private Time time;

    @Autowired
    private SupportingEvidenceValidatorService supportingEvidenceValidatorService;

    @Test
    void shouldReturnNoValidationErrorsWhenDatesOnSupportingEvidenceBundleAreInTheFuture() {
        LocalDateTime futureDate = time.now().plusDays(2);
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle = buildSupportingEvidenceBundle(futureDate);

        when(validateGroupService.validateGroup(supportingEvidenceBundle, DateOfIssueGroup.class))
            .thenReturn(List.of());

        List<String> validationErrors = supportingEvidenceValidatorService.validate(supportingEvidenceBundle);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnValidationErrorsWhenDatesOnSupportingEvidenceBundleAreInThePast() {
        LocalDateTime pastDate = time.now().minusDays(2);
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle = buildSupportingEvidenceBundle(pastDate);

        when(validateGroupService.validateGroup(supportingEvidenceBundle, DateOfIssueGroup.class))
            .thenReturn(of(VALIDATION_ERROR));

        List<String> validationErrors = supportingEvidenceValidatorService.validate(supportingEvidenceBundle);

        assertThat(validationErrors).containsExactly(VALIDATION_ERROR);
    }

    @Test
    void shouldReturnValidationErrorsWhenNoDatesAreSetOnSupportingEvidenceBundle() {
        SupportingEvidenceBundle supportingEvidenceBundle = SupportingEvidenceBundle.builder().build();
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundleList
            = List.of(element(supportingEvidenceBundle));

        when(validateGroupService.validateGroup(supportingEvidenceBundle, DateOfIssueGroup.class))
            .thenReturn(of(VALIDATION_ERROR));

        List<String> validationErrors = supportingEvidenceValidatorService.validate(supportingEvidenceBundleList);

        assertThat(validationErrors).containsExactly(VALIDATION_ERROR);
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle(LocalDateTime dateTimeReceived) {
        return List.of(
            element(SupportingEvidenceBundle.builder()
                .dateTimeReceived(time.now().minusDays(2))
                .build())
        );
    }
}
