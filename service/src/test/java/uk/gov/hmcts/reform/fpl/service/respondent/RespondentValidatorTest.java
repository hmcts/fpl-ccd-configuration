package uk.gov.hmcts.reform.fpl.service.respondent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
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
    private static final List<String> TELEPHONES = List.of("1234 567 897");
    public static final LocalDateTime NOW = LocalDateTime.of(2012, 6, 20, 12, 0, 0);

    @Mock
    private Time time;

    @Mock
    private RespondentService respondentService;

    @Mock
    private ValidateEmailService validateEmailService;

    @Mock
    private RespondentAfterSubmissionValidator respondentAfterSubmissionValidator;

    @InjectMocks
    private RespondentValidator underTest;

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(NOW);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnErrorWhenMaximumRespondentsExceeded(Boolean hideRespondentIndex) {
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

        List<String> actual = underTest.validate(caseData, CASE_DATA_BEFORE, hideRespondentIndex);

        assertThat(actual).isEqualTo(List.of(
            "Maximum number of respondents is 10",
            "emailValidatorError"));
    }

    private static Stream<Arguments> shouldReturnErrorWhenDateOfBirthInTheFutureParam() {
        return Stream.of(
            Arguments.of(false, new String[] {
                "Date of birth for respondent 1 cannot be in the future",
                "emailValidatorError"
            }),
            Arguments.of(true, new String[] {
                "Date of birth for respondent cannot be in the future",
                "emailValidatorError"
            })
        );
    }

    @ParameterizedTest
    @MethodSource("shouldReturnErrorWhenDateOfBirthInTheFutureParam")
    void shouldReturnErrorWhenDateOfBirthInTheFuture(boolean hideRespondentIndex, String[] messages) {
        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(NOW.toLocalDate().plusDays(1))
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(element(respondent)))
            .build();

        mockServices(caseData);

        List<String> actual = underTest.validate(caseData, CASE_DATA_BEFORE, hideRespondentIndex);

        assertThat(actual).isEqualTo(List.of(messages));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldShowAfterSubmissionErrors(boolean hideRespondentIndex) {
        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(NOW.toLocalDate().minusDays(1))
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .respondents1(List.of(element(respondent)))
            .build();

        mockServices(caseData);
        when(respondentAfterSubmissionValidator.validate(eq(caseData), eq(CASE_DATA_BEFORE), isA(Boolean.class)))
            .thenReturn(List.of("afterSubmissionValidatorError"));

        List<String> actual = underTest.validate(caseData, CASE_DATA_BEFORE, hideRespondentIndex);

        assertThat(actual).isEqualTo(List.of(
            "emailValidatorError",
            "afterSubmissionValidatorError"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotShowAfterSubmissionErrorsWhenStateIsOpen(boolean hideRespondentIndex) {
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

        List<String> actual = underTest.validate(caseData, CASE_DATA_BEFORE, hideRespondentIndex);

        assertThat(actual).isEqualTo(List.of(
            "emailValidatorError"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnErrorWhenMissingAddress(boolean hideRespondentIndex) {
        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(NOW.toLocalDate().minusDays(1))
                .addressKnow(IsAddressKnowType.YES)
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(
                element(respondent), element(respondent)))
            .build();

        mockServices(caseData);

        List<String> actual = underTest.validate(caseData, CASE_DATA_BEFORE, hideRespondentIndex);

        assertThat(actual).isEqualTo(List.of(
            "Enter respondent's address",
            "Enter respondent's address",
            "emailValidatorError"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnErrorWhenMissingAddressFields(boolean hideRespondentIndex) {
        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(NOW.toLocalDate().minusDays(1))
                .addressKnow(IsAddressKnowType.YES)
                .address(Address.builder().build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(
                element(respondent)))
            .build();

        mockServices(caseData);

        List<String> actual = underTest.validate(caseData, CASE_DATA_BEFORE, hideRespondentIndex);

        assertThat(actual).isEqualTo(List.of(
            "Building and Street is required",
            "Town or City is required",
            "Postcode/Zipcode is required",
            "Country is required",
            "emailValidatorError"));
    }

    private void mockServices(CaseData caseData) {
        when(respondentService.getRespondentsWithLegalRepresentation(caseData.getRespondents1()))
            .thenReturn(RESPONDENTS);

        when(respondentService.getRespondentSolicitorEmails(RESPONDENTS))
            .thenReturn(EMAILS);
        when(respondentService.getRespondentSolicitorTelephones(RESPONDENTS))
            .thenReturn(TELEPHONES);

        when(validateEmailService.validate(EMAILS, "Representative"))
            .thenReturn(List.of("emailValidatorError"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnErrorWhenMissingLegalRepresentativeTelephoneNumber(boolean hideRespondentIndex) {
        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(NOW.toLocalDate().minusDays(1))
                .addressKnow(IsAddressKnowType.YES)
                .address(Address.builder()
                    .addressLine1("Line 1")
                    .postTown("Town")
                    .postcode("GU1 FFF")
                    .country("United Kingdom")
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(element(respondent), element(respondent)))
            .build();

        when(respondentService.getRespondentsWithLegalRepresentation(caseData.getRespondents1()))
            .thenReturn(RESPONDENTS);
        when(respondentService.getRespondentSolicitorTelephones(RESPONDENTS))
            .thenReturn(List.of());

        List<String> actual = underTest.validate(caseData, CASE_DATA_BEFORE, hideRespondentIndex);

        assertThat(actual).isEqualTo(List.of(
            "Telephone number of legal representative is required."));
    }
}
