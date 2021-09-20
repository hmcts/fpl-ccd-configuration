package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.HMCTS_ADMIN;
import static uk.gov.hmcts.reform.fpl.enums.UserRole.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper.respondentWithSolicitor;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerMidEventTest extends AbstractCallbackTest {

    private static final String DOB_ERROR = "Date of birth for respondent 1 cannot be in the future";
    private static final String DOB_ERROR_2 = "Date of birth for respondent 2 cannot be in the future";
    private static final String MAX_RESPONDENTS_ERROR = "Maximum number of respondents is 10";

    RespondentControllerMidEventTest() {
        super("enter-respondents");
    }

    @Test
    void shouldReturnMaximumRespondentErrorsWhenNumberOfRespondentsExceeds10() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(
                respondent(dateNow()), respondent(dateNow()), respondent(dateNow()), respondent(dateNow()),
                respondent(dateNow()), respondent(dateNow()), respondent(dateNow()), respondent(dateNow()),
                respondent(dateNow()), respondent(dateNow()), respondent(dateNow()), respondent(dateNow())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains(MAX_RESPONDENTS_ERROR);
    }

    @Test
    void shouldReturnDateOfBirthErrorsForRespondentWhenFutureDateOfBirth() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondent(dateNow().plusDays(1))))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains(DOB_ERROR);
    }

    @Test
    void shouldReturnDateOfBirthErrorsForRespondentWhenThereIsMultipleRespondents() {
        CaseData caseData = CaseData.builder()
            .respondents1(buildRespondents())
            .state(OPEN)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).containsExactly(DOB_ERROR, DOB_ERROR_2);
    }

    @Test
    void shouldReturnNoDateOfBirthErrorsForRespondentWhenValidDateOfBirth() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondent(dateNow().minusDays(1))))
            .state(OPEN)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnEmailAddressErrorsForRespondentSolicitorEmailWhenInvalid() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondentWithSolicitor(dateNow().plusDays(1), "Test User <e.test@test.com>")))
            .state(OPEN)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(DOB_ERROR,
            "Representative 1: Enter an email address in the correct format, for example name@example.com");
    }

    @Test
    void shouldReturnEmailAddressErrorsWhenThereAreMultipleRespondentSolicitors() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(
                respondentWithSolicitor(dateNow(), "Test User <e.test@test.com>"),
                respondentWithSolicitor(dateNow(), "Second Test User <e.test-second@test.com>")
            ))
            .state(OPEN)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).containsExactly("Representative 1: Enter an email address "
                + "in the correct format, for example name@example.com",
            "Representative 2: Enter an email address in the correct format, for example name@example.com");
    }

    @Test
    void shouldReturnNoEmailErrorsForRespondentSolicitorWhenValidEmail() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondentWithSolicitor(dateNow(), "test@test.com")))
            .state(OPEN)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnRespondentRemovedValidationErrorsWhenRespondentRemoved() {
        CaseData caseDataBefore = CaseData.builder()
            .respondents1(List.of(element(respondentWithSolicitor(dateNow(), "test@test.com"))))
            .state(SUBMITTED)
            .build();

        CaseData caseData = CaseData.builder()
            .respondents1(List.of())
            .build();

        CallbackRequest callbackRequest = toCallBackRequest(caseData, caseDataBefore);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(callbackRequest);

        assertThat(callbackResponse.getErrors()).isEqualTo(List.of("You cannot remove a respondent from the case"));
    }

    @Test
    void shouldAllowAdminToUpdateRespondentSolicitorOrganisation() {

        Element<Respondent> respondent = element(respondentWithSolicitor(dateNow(), "test@test.com"));
        Element<Respondent> updatedRespondent = element(respondent.getId(), respondent.getValue().toBuilder()
            .solicitor(respondent.getValue().getSolicitor().toBuilder()
                .firstName("James")
                .lastName("Smith")
                .email("test@mail.com")
                .organisation(Organisation.organisation("NEW"))
                .build())
            .build());

        CaseData caseDataBefore = CaseData.builder()
            .respondents1(List.of(respondent))
            .state(SUBMITTED)
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .respondents1(List.of(updatedRespondent))
            .build();

        CallbackRequest callbackRequest = toCallBackRequest(caseData, caseDataBefore);

        List<String> errors = postMidEventWithUserRole(callbackRequest, HMCTS_ADMIN.getRoleName()).getErrors();

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotAllowLocalAuthorityToUpdateRespondentSolicitorOrganisation() {

        Element<Respondent> respondent = element(respondentWithSolicitor(dateNow(), "respondent1@test.com"));
        Element<Respondent> updatedRespondent = element(respondent.getId(), respondent.getValue().toBuilder()
            .solicitor(respondent.getValue().getSolicitor().toBuilder()
                .firstName("James")
                .lastName("Smith")
                .email("test@mail.com")
                .organisation(Organisation.organisation("NEW_ORG"))
                .build())
            .build());

        CaseData caseDataBefore = CaseData.builder()
            .respondents1(List.of(respondent))
            .state(SUBMITTED)
            .build();

        CaseData caseData = caseDataBefore.toBuilder()
            .respondents1(List.of(updatedRespondent))
            .build();

        CallbackRequest callbackRequest = toCallBackRequest(caseData, caseDataBefore);

        List<String> errors = postMidEventWithUserRole(callbackRequest, LOCAL_AUTHORITY.getRoleName()).getErrors();

        assertThat(errors)
            .isEqualTo(List.of("You cannot change organisation details for respondent 1's legal representative"));
    }

    private List<Element<Respondent>> buildRespondents() {
        return wrapElements(respondent(dateNow().plusDays(1)), respondent(dateNow().plusDays(1)));
    }

    private Respondent respondent(LocalDate dateOfBirth) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(dateOfBirth)
                .build())
            .build();
    }
}
