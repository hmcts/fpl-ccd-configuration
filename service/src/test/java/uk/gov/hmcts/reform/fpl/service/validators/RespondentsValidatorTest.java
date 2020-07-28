package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RespondentsValidator.class, LocalValidatorFactoryBean.class})
class RespondentsValidatorTest {

    @Autowired
    private RespondentsValidator respondentsValidator;

    @Test
    void shouldReturnErrorWhenNoRespondentsSpecified() {

        final CaseData caseData = CaseData.builder().build();

        final List<String> errors = respondentsValidator.validate(caseData);

        assertThat(errors).contains("Add the respondent's details");
    }

    @Test
    void shouldReturnErrorsWhenNoRespondentsDetailsSpecified() {

        final Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder().build())
            .build();

        final CaseData caseData = CaseData.builder()
            .respondents1(ElementUtils.wrapElements(respondent))
            .build();

        final List<String> errors = respondentsValidator.validate(caseData);

        assertThat(errors).containsExactlyInAnyOrder(
            "Enter the respondent's relationship to child",
            "Enter the respondent's full name"
        );
    }

    @Test
    void shouldReturnEmptyErrorsWhenRequiredRespondentsDetailsArePresentAndValid() {

        final Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("John")
                .lastName("Smith")
                .relationshipToChild("Uncle")
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .respondents1(ElementUtils.wrapElements(respondent))
            .build();

        final List<String> errors = respondentsValidator.validate(caseData);

        assertThat(errors).isEmpty();
    }
}
