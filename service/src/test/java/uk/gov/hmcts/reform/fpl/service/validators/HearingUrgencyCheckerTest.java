package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HearingUrgencyChecker.class, LocalValidatorFactoryBean.class})
class HearingUrgencyCheckerTest {

    @Autowired
    private HearingUrgencyChecker hearingUrgencyChecker;

    @Test
    void shouldReturnErrorWhenNoHearingNeedsProvided() {
        final CaseData caseData = CaseData.builder().build();

        final List<String> errors = hearingUrgencyChecker.validate(caseData);
        final boolean isCompleted = hearingUrgencyChecker.isCompleted(caseData);

        assertThat(errors).containsExactly("Add the hearing urgency details");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnErrorWhenHearingTimeFrameIsNotProvided() {
        final Hearing hearing = Hearing.builder().build();
        final CaseData caseData = CaseData.builder()
                .hearing(hearing)
                .build();

        final List<String> errors = hearingUrgencyChecker.validate(caseData);
        final boolean isCompleted = hearingUrgencyChecker.isCompleted(caseData);

        assertThat(errors).containsExactly("Select an option for when you need a hearing");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnEmptyErrorsWhenHearingTimeFrameIsProvided() {
        final Hearing hearing = Hearing.builder()
                .timeFrame("Within 18 days")
                .build();
        final CaseData caseData = CaseData.builder()
                .hearing(hearing)
                .build();

        final List<String> errors = hearingUrgencyChecker.validate(caseData);
        final boolean isCompleted = hearingUrgencyChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isFalse();
    }
}
