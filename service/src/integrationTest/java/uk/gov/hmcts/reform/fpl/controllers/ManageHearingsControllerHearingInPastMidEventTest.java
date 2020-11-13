package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@OverrideAutoConfiguration(enabled = true)
@WebMvcTest(ManageHearingsController.class)
public class ManageHearingsControllerHearingInPastMidEventTest extends AbstractControllerTest {

    private static final String CASE_ID = "12345";

    private LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

    ManageHearingsControllerHearingInPastMidEventTest() {
        super("manage-hearings");
    }

    @Test
    void shouldCorrectBothHearingDatesToCorrectOneWhenIncorrectAndPastHearingDatesIsEnabled() {
        LocalDateTime correctStartDate = LocalDateTime.now().minusDays(2);
        LocalDateTime correctEndDate = LocalDateTime.now().minusDays(1);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", pastDate,
                "hearingStartDateConfirmation", correctStartDate,
                "hearingEndDateConfirmation", correctEndDate,
                "confirmHearingDate","No"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "hearing-in-past");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData.get("hearingStartDate").equals(correctStartDate));
        assertThat(responseData.get("hearingEndDate").equals(correctEndDate));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hearingEndDateConfirmation", "hearingStartDateConfirmation"})
    void shouldCorrectHearingDateWhenIncorrect(String hearingDateConfirmation) {
        LocalDateTime correctDate = LocalDateTime.now().minusDays(2);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", pastDate,
                hearingDateConfirmation, correctDate,
                "confirmHearingDate","No"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "hearing-in-past");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData.get("hearingStartDate").equals(pastDate));
        assertThat(responseData.get("hearingEndDate").equals(correctDate));
    }

    @Test
    void shouldNotChangeHearingDateIfCorrect() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "confirmHearingDate","Yes"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "hearing-in-past");

        Map<String, Object> responseData = callbackResponse.getData();

        assertThat(responseData.get("hearingStartDate").equals(pastDate));
    }

    @Test
    void shouldThrowValidationErrorsWhenCorrectedHearingDateTimeIsInvalid() {
        LocalDateTime correctStartDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        LocalDateTime correctEndDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", pastDate,
                "hearingStartDateConfirmation", correctStartDate,
                "hearingEndDateConfirmation", correctEndDate,
                "confirmHearingDate","No"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "hearing-in-past");

        assertThat(callbackResponse.getErrors().contains("Enter a valid start time"));
        assertThat(callbackResponse.getErrors().contains("Enter a valid end time"));
    }

    @Test
    void shouldNotThrowValidationErrorsWhenCorrectedHearingDateTimeIsValid() {
        LocalDateTime correctStartDate = LocalDateTime.now().plusDays(2);
        LocalDateTime correctEndDate = LocalDateTime.now().plusDays(2);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(parseLong(CASE_ID))
            .data(Map.of("hearingStartDate", pastDate,
                "hearingEndDate", pastDate,
                "hearingStartDateConfirmation", correctStartDate,
                "hearingEndDateConfirmation", correctEndDate,
                "confirmHearingDate","No"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails, "hearing-in-past");

        assertThat(callbackResponse.getErrors().isEmpty());
    }
}
