package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentLocalAuthority;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.service.respondent.RespondentAfterSubmissionValidator;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Respondents3rdPartyChecker.class, LocalValidatorFactoryBean.class,
    RespondentAfterSubmissionValidator.class})
class Respondents3rdPartyCheckerTest {

    @Autowired
    private Respondents3rdPartyChecker respondents3rdPartyChecker;

    @MockBean
    private RespondentAfterSubmissionValidator respondentAfterSubmissionValidator;

    private static final RespondentLocalAuthority FULL_RESPONDENT_LA = RespondentLocalAuthority.builder()
        .name("Swansea County Council")
                .address(Address.builder().build())
        .representativeFirstName("John")
                .representativeLastName("Smith")
                .email("test@test.com")
                .phoneNumber("1234")
                .usingOtherOrg(YesNo.NO)
                .build();

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
    void shouldReturnErrorWhenNoLaywerLastName() {
        final CaseData caseData = CaseData.builder()
            .respondentLocalAuthority(RespondentLocalAuthority.builder()
                .representativeFirstName("John")
                .representativeLastName(null)
                .build())
            .build();

        final List<String> errors = respondents3rdPartyChecker.validate(caseData);
        final boolean isCompleted = respondents3rdPartyChecker.isCompleted(caseData);

        assertThat(errors).contains("Respondent Local Authority lawyer is required");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnErrorWhenNoLaywerFirstName() {
        final CaseData caseData = CaseData.builder()
            .respondentLocalAuthority(RespondentLocalAuthority.builder()
                .representativeFirstName(null)
                .representativeLastName("Smith")
                .build())
            .build();

        final List<String> errors = respondents3rdPartyChecker.validate(caseData);
        final boolean isCompleted = respondents3rdPartyChecker.isCompleted(caseData);

        assertThat(errors).contains("Respondent Local Authority lawyer is required");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldReturnErrorWhenNeedsOutsourcingButDoesntHaveOutsourcingOrgSet() {
        final CaseData caseData = CaseData.builder()
            .respondentLocalAuthority(RespondentLocalAuthority.builder()
                .usingOtherOrg(YesNo.YES)
                .organisation(null)
                .build())
            .build();

        final List<String> errors = respondents3rdPartyChecker.validate(caseData);
        final boolean isCompleted = respondents3rdPartyChecker.isCompleted(caseData);

        assertThat(errors).contains("Respondent Local Authority outsourcing organisation is required");
        assertThat(isCompleted).isFalse();
    }

    @Test
    void shouldHaveNoErrorsIfAllMandatoryFieldsFilledIn() {
        final CaseData caseData = CaseData.builder()
            .respondentLocalAuthority(FULL_RESPONDENT_LA)
            .build();

        final List<String> errors = respondents3rdPartyChecker.validate(caseData);
        final boolean isCompleted = respondents3rdPartyChecker.isCompleted(caseData);

        assertThat(errors).isEmpty();
        assertThat(isCompleted).isTrue();
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

    @Test
    void shouldReturnFalseForIsStarted() {
        assertThat(respondents3rdPartyChecker.isStarted(CaseData.builder().build())).isFalse();
    }

    @Nested
    class RespondentsChecker {

        @Test
        void shouldNotReturnErrorsWhenNoLegalRepresentationNeeded() {
            final Respondent respondent = Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("John")
                    .lastName("Smith")
                    .relationshipToChild("Uncle")
                    .build())
                .legalRepresentation(NO.getValue())
                .isLocalAuthority(NO)
                .build();
            final CaseData caseData = CaseData.builder()
                .respondentLocalAuthority(FULL_RESPONDENT_LA)
                .respondents1(ElementUtils.wrapElements(respondent))
                .build();

            final List<String> errors = respondents3rdPartyChecker.validate(caseData);
            final boolean isCompleted = respondents3rdPartyChecker.isCompleted(caseData);

            assertThat(errors).isEmpty();
            assertThat(isCompleted).isTrue();
        }

    }

}
