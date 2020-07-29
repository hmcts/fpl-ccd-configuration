package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Risks;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RiskAndHarmValidator.class})
class RiskAndHarmValidatorTest {

    @Autowired
    private RiskAndHarmValidator riskAndHarmValidator;

    @Test
    void shouldReturnErrorWhenRiskNorHarmNotProvided() {
        final CaseData caseData = CaseData.builder()
            .build();

        final List<String> errors = riskAndHarmValidator.validate(caseData);

        assertThat(errors).containsExactly("You need to add risks and harms for children");
    }

    @Test
    void shouldReturnErrorWhenRiskNorHarmDetailsNotProvided() {
        final CaseData caseData = CaseData.builder()
            .risks(Risks.builder().build())
            .build();

        final List<String> errors = riskAndHarmValidator.validate(caseData);

        assertThat(errors).containsExactly("You need to add risks and harms for children");
    }

    @Test
    void shouldReturnEmptyErrorsWhenAtLeastEmotionalHarmIsProvided() {
        final CaseData caseData = CaseData.builder()
            .risks(Risks.builder()
                .emotionalHarm("Yes")
                .build())
            .build();

        final List<String> errors = riskAndHarmValidator.validate(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnEmptyErrorsWhenAtLeastPhysicalHarmIsProvided() {
        final CaseData caseData = CaseData.builder()
            .risks(Risks.builder()
                .physicalHarm("No")
                .build())
            .build();

        final List<String> errors = riskAndHarmValidator.validate(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnEmptyErrorsWhenAtLeastSexualAbuseIsProvided() {
        final CaseData caseData = CaseData.builder()
            .risks(Risks.builder()
                .sexualAbuse("Yes")
                .build())
            .build();

        final List<String> errors = riskAndHarmValidator.validate(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnEmptyErrorsWhenAtLeastNeglectIsSelected() {
        final CaseData caseData = CaseData.builder()
            .risks(Risks.builder()
                .neglect("No")
                .build())
            .build();

        final List<String> errors = riskAndHarmValidator.validate(caseData);

        assertThat(errors).isEmpty();
    }
}
