package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentLocalAuthority;
import uk.gov.hmcts.reform.fpl.service.respondent.RespondentAfterSubmissionValidator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Respondents3rdPartyChecker.class, LocalValidatorFactoryBean.class,
    RespondentAfterSubmissionValidator.class})
class Respondents3rdPartyCheckerTest {

    @Autowired
    private Respondents3rdPartyChecker respondents3rdPartyChecker;

    @MockBean
    private RespondentAfterSubmissionValidator respondentAfterSubmissionValidator;

    @BeforeEach
    void validatorMock() {
        given(respondentAfterSubmissionValidator.validateLegalRepresentation(any())).willReturn(List.of());
    }

    @Test
    void shouldReturnErrorWhenNoLADetails() {
        final CaseData caseData = CaseData.builder()
            .respondentLocalAuthority(RespondentLocalAuthority.builder().build())
            .build();

        final List<String> errors = respondents3rdPartyChecker.validate(caseData);
        final boolean isCompleted = respondents3rdPartyChecker.isCompleted(caseData);

        assertThat(errors).contains("Respondent Local Authority address is required",
            "Respondent Local Authority email is required",
            "Respondent Local Authority lawyer is required");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnErrorWhenNoLAObject() {
        final CaseData caseData = CaseData.builder()
            .build();

        final List<String> errors = respondents3rdPartyChecker.validate(caseData);
        final boolean isCompleted = respondents3rdPartyChecker.isCompleted(caseData);

        assertThat(errors).contains("Respondent Local Authority details need to be added");
        assertThat(isCompleted).isFalse();
    }

}
