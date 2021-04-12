package uk.gov.hmcts.reform.fpl.service.respondent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class RespondentValidatorTest {

    private static final CaseData CASE_DATA_BEFORE = mock(CaseData.class);
    private static final List<Respondent> RESPONDENTS = List.of(mock(Respondent.class));
    private static final List<String> EMAILS = List.of("test@example.com");
    public static final LocalDateTime NOW = LocalDateTime.of(2012, 6, 20, 12, 0, 0);

    @Mock
    private Time time;

    @Mock
    private RespondentService respondentService;

    @Mock
    private ValidateEmailService validateEmailService;

    @Mock
    private RespondentAfterSubmissionValidator respondentAfterSubmissionValidator;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private RespondentValidator underTest;

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(NOW);
        when(featureToggleService.hasRSOCaseAccess()).thenReturn(false);
    }

    @Test
    void shouldReturnErrorWhenMaximumRespondentsExceeded() {
        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(NOW.toLocalDate().minusDays(1))
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(
                element(respondent), element(respondent), element(respondent), element(respondent),
                element(respondent), element(respondent), element(respondent), element(respondent),
                element(respondent), element(respondent), element(respondent)))
            .build();

        mockServices(caseData);

        List<String> actual = underTest.validate(caseData, CASE_DATA_BEFORE);

        assertThat(actual).isEqualTo(List.of(
            "Maximum number of respondents is 10",
            "errorEmailValidator"));
    }

    @Test
    void shouldReturnErrorWhenDateOfBirthInTheFuture() {
        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(NOW.toLocalDate().plusDays(1))
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(element(respondent)))
            .build();

        mockServices(caseData);

        List<String> actual = underTest.validate(caseData, CASE_DATA_BEFORE);

        assertThat(actual).isEqualTo(List.of(
            "Date of birth cannot be in the future",
            "errorEmailValidator"));
    }

    @Test
    void shouldShowAfterSubmissionErrorsWhenToggledOn() {
        when(featureToggleService.hasRSOCaseAccess()).thenReturn(true);

        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(NOW.toLocalDate().minusDays(1))
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .respondents1(List.of(
                element(respondent)))
            .build();

        mockServices(caseData);

        when(respondentAfterSubmissionValidator.validate(caseData, CASE_DATA_BEFORE))
            .thenReturn(List.of("errorAfterSubmissionValidator"));

        List<String> actual = underTest.validate(caseData, CASE_DATA_BEFORE);

        assertThat(actual).isEqualTo(List.of(
            "errorEmailValidator",
            "errorAfterSubmissionValidator"));
    }

    @Test
    void shouldNotShowAfterSubmissionErrorsWhenStateIsOpen() {
        when(featureToggleService.hasRSOCaseAccess()).thenReturn(true);

        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(NOW.toLocalDate().minusDays(1))
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .state(OPEN)
            .respondents1(List.of(
                element(respondent)))
            .build();

        mockServices(caseData);

        List<String> actual = underTest.validate(caseData, CASE_DATA_BEFORE);

        assertThat(actual).isEqualTo(List.of(
            "errorEmailValidator"));
    }

    private void mockServices(CaseData caseData) {
        when(respondentService.getRespondentsWithLegalRepresentation(caseData.getRespondents1()))
            .thenReturn(RESPONDENTS);

        when(respondentService.getRespondentSolicitorEmails(RESPONDENTS))
            .thenReturn(EMAILS);

        when(validateEmailService.validate(EMAILS, "Representative"))
            .thenReturn(List.of("errorEmailValidator"));
    }
}
