package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerTest extends AbstractControllerTest {

    RespondentControllerTest() {
        super("enter-respondents");
    }

    private static final String ERROR_MESSAGE = "Date of birth cannot be in the future";

    @Test
    void aboutToStartShouldPrepopulateRespondent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("respondents1");
    }

    @Test
    void shouldReturnDateOfBirthErrorsForRespondentWhenFutureDateOfBirth() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("respondents1", wrapElements(Respondent.builder()
                .party(RespondentParty.builder()
                    .dateOfBirth(LocalDate.now().plusDays(1))
                    .build())
                .build())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnDateOfBirthErrorsForRespondentWhenThereIsMultipleRespondents() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("respondents1", wrapElements(Respondent.builder()
                    .party(RespondentParty.builder()
                        .dateOfBirth(LocalDate.now().plusDays(1))
                        .build())
                    .build(),
                Respondent.builder()
                    .party(RespondentParty.builder()
                        .dateOfBirth(LocalDate.now().plusDays(1))
                        .build())
                    .build())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).containsExactly(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoDateOfBirthErrorsForRespondentWhenValidDateOfBirth() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("respondents1", wrapElements(Respondent.builder()
                .party(RespondentParty.builder()
                    .dateOfBirth(LocalDate.now().minusDays(1))
                    .build())
                .build())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void aboutToSubmitShouldAddConfidentialRespondentsToCaseDataWhenConfidentialRespondentsExist() throws Exception {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest());
        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        CaseData initialData = mapper.convertValue(callbackRequest().getCaseDetails().getData(), CaseData.class);

        assertThat(caseData.getConfidentialRespondents())
            .containsOnly(retainConfidentialDetails(initialData.getAllRespondents().get(0)));

        assertThat(caseData.getRespondents1().get(0).getValue().getParty().getAddress()).isNull();
        assertThat(caseData.getRespondents1().get(1).getValue().getParty().getAddress()).isNotNull();
    }

    private Element<Respondent> retainConfidentialDetails(Element<Respondent> respondent) {
        return element(respondent.getId(), Respondent.builder()
            .party(RespondentParty.builder()
                .firstName(respondent.getValue().getParty().getFirstName())
                .lastName(respondent.getValue().getParty().getLastName())
                .address(respondent.getValue().getParty().getAddress())
                .telephoneNumber(respondent.getValue().getParty().getTelephoneNumber())
                .email(respondent.getValue().getParty().getEmail())
                .build())
            .build());
    }
}
