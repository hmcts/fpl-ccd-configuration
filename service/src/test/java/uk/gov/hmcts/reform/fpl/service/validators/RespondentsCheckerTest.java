package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RespondentsChecker.class, LocalValidatorFactoryBean.class})
class RespondentsCheckerTest {

    @Autowired
    private RespondentsChecker respondentsChecker;

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnErrorWhenNoRespondentsSpecified(List<Element<Respondent>> respondents) {
        final CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .build();

        final List<String> errors = respondentsChecker.validate(caseData);
        final boolean isCompleted = respondentsChecker.isCompleted(caseData);

        assertThat(errors).contains("Add the respondents' details");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnErrorsWhenNoRespondentsDetailsSpecified() {
        final Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder().build())
            .build();
        final CaseData caseData = CaseData.builder()
            .respondents1(ElementUtils.wrapElements(respondent))
            .build();

        final List<String> errors = respondentsChecker.validate(caseData);
        final boolean isCompleted = respondentsChecker.isCompleted(caseData);

        assertThat(errors).containsExactlyInAnyOrder(
            "Enter the respondent's relationship to child",
            "Enter the respondent's full name",
            "Select if the respondent needs representation"
        );
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnEmptyErrorsWhenRequiredRespondentsDetailsArePresentAndValid() {
        final Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("John")
                .lastName("Smith")
                .relationshipToChild("Uncle")
                .build())
            .legalRepresentation(YES.getValue())
            .build();
        final CaseData caseData = CaseData.builder()
            .respondents1(ElementUtils.wrapElements(respondent))
            .build();

        final List<String> errors = respondentsChecker.validate(caseData);
        final boolean isCompleted = respondentsChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
    }
}
