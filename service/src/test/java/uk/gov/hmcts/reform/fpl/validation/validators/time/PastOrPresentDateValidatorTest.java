package uk.gov.hmcts.reform.fpl.validation.validators.time;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {FixedTimeConfiguration.class})
class PastOrPresentDateValidatorTest extends TimeValidatorTest {

    @Autowired
    private Time time;

    @Test
    void shouldReturnAnErrorWhenDateIsInFuture() {
        SupportingEvidenceBundle evidence = SupportingEvidenceBundle.builder()
            .dateTimeReceived(FUTURE)
            .build();

        final List<String> violations = validate(evidence);

        assertThat(violations).containsOnly("Date received cannot be in the future");
    }

    @Test
    void shouldNotReturnAnErrorWhenDateIsToday() {
        SupportingEvidenceBundle evidence = SupportingEvidenceBundle.builder()
            .dateTimeReceived(time.now())
            .build();

        final List<String> violations = validate(evidence);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorWhenDateIsInPast() {
        SupportingEvidenceBundle evidence = SupportingEvidenceBundle.builder()
            .dateTimeReceived(time.now().minusDays(2))
            .build();

        final List<String> violations = validate(evidence);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenDateIsNull() {
        SupportingEvidenceBundle evidence = SupportingEvidenceBundle.builder()
            .dateTimeReceived(null)
            .build();

        final List<String> violations = validate(evidence);

        assertThat(violations).isEmpty();
    }
}
