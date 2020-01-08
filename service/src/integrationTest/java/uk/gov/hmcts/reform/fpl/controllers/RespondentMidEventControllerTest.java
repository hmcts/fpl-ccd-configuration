package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentMidEventControllerTest extends AbstractControllerTest {

    private static final String ERROR_MESSAGE = "Date of birth cannot be in the future";

    RespondentMidEventControllerTest() {
        super("enter-respondents");
    }

    @Test
    void shouldReturnDateOfBirthErrorsForRespondentWhenFutureDateOfBirth() {
        CaseDetails caseDetails = caseWithRespondents(now().plusDays(1));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnDateOfBirthErrorsForRespondentWhenThereIsMultipleRespondents() {
        CaseDetails caseDetails = caseWithRespondents(now().plusDays(1), now().plusDays(1));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).containsExactly(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoDateOfBirthErrorsForRespondentWhenValidDateOfBirth() {
        CaseDetails caseDetails = caseWithRespondents(now().minusDays(1));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    private CaseDetails caseWithRespondents(LocalDate... respondentsDob) {
        List<Map<String, Object>> respondents = Stream.of(respondentsDob)
            .map(dob -> Map.of(
                "id", "",
                "value", Respondent.builder()
                    .party(RespondentParty.builder()
                        .dateOfBirth(dob)
                        .build())
                    .build()
            )).collect(Collectors.toList());

        return CaseDetails.builder()
            .id(12345L)
            .data(Map.of("respondents1", respondents))
            .build();
    }
}
