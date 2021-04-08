package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerTest extends AbstractCallbackTest {

    RespondentControllerTest() {
        super("enter-respondents");
    }

    private static final String DOB_ERROR_MESSAGE = "Date of birth cannot be in the future";
    private static final String SOLICITOR_NAME_ERROR_MESSAGE = "Enter a representative name";

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
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondent(dateNow().plusDays(1))))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains(DOB_ERROR_MESSAGE);
    }

    @Test
    void shouldReturnDateOfBirthErrorsForRespondentWhenThereIsMultipleRespondents() {
        CaseData caseData = CaseData.builder()
            .respondents1(buildRespondents())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).containsExactly(DOB_ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoDateOfBirthErrorsForRespondentWhenValidDateOfBirth() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondent(dateNow().minusDays(1))))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnRespondentSolicictorNameErrorWhenInvalidName() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(
                Respondent.builder()
                    .party(RespondentParty.builder().dateOfBirth(dateNow()).build())
                    .legalRepresentation(YES.getValue())
                    .solicitor(RespondentSolicitor.builder().lastName(null).build())
                    .build())
            ).build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains(SOLICITOR_NAME_ERROR_MESSAGE);
    }

    @Test
    void shouldReturnRespondentSolicictorNameErrorForRespondentWhenThereIsMultipleRespondents() {
        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(dateNow())
                .build())
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .email("test@test.com")
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondent, respondent))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains(SOLICITOR_NAME_ERROR_MESSAGE);
    }

    @Test
    void shouldReturnRespondentSolicictorNameErrorWhenValidName() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondent(dateNow(), "test@test.com")))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnEmailAddressErrorsForRespondentSolicitorEmailWhenInvalid() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondent(dateNow().plusDays(1), "Test User <e.test@test.com>")))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(DOB_ERROR_MESSAGE,
            "Representative 1: Enter an email address in the correct format, for example name@example.com");
    }

    @Test
    void shouldReturnEmailAddressErrorsWhenThereAreMultipleRespondentSolicitors() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(
                respondent(dateNow(), "Test User <e.test@test.com>"),
                respondent(dateNow(), "Second Test User <e.test-second@test.com>")
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).containsExactly("Representative 1: Enter an email address "
                + "in the correct format, for example name@example.com",
            "Representative 2: Enter an email address in the correct format, for example name@example.com");
    }

    @Test
    void shouldReturnNoEmailErrorsForRespondentSolicitorWhenValidEmail() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondent(dateNow(), "test@test.com")))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }


    @Test
    void shouldPersistRepresentativeAssociation() {
        List<Element<UUID>> association = List.of(element(UUID.randomUUID()));
        Element<Respondent> oldRespondent = element(respondent(dateNow()));
        oldRespondent.getValue().setRepresentedBy(association);

        CaseData caseData = CaseData.builder()
            .respondents1(List.of(
                element(oldRespondent.getId(), respondent(dateNow())),
                element(respondent(dateNow()))
            ))
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .respondents1(List.of(oldRespondent))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
            toCallBackRequest(asCaseDetails(caseData), asCaseDetails(caseDataBefore))
        );
        CaseData responseData = extractCaseData(response);

        Respondent firstRespondent = responseData.getRespondents1().get(0).getValue();
        Respondent secondRespondent = responseData.getRespondents1().get(1).getValue();

        assertThat(firstRespondent.getRepresentedBy()).isEqualTo(association);
        assertThat(secondRespondent.getRepresentedBy()).isNullOrEmpty();
    }

    @Test
    void aboutToSubmitShouldAddConfidentialRespondentsToCaseDataWhenConfidentialRespondentsExist() {
        CallbackRequest callbackRequest = callbackRequest();
        CaseData caseData = extractCaseData(postAboutToSubmitEvent(callbackRequest));
        CaseData initialData = mapper.convertValue(callbackRequest.getCaseDetails().getData(), CaseData.class);

        assertThat(caseData.getConfidentialRespondents())
            .containsOnly(retainConfidentialDetails(initialData.getAllRespondents().get(0)));

        assertThat(caseData.getRespondents1().get(0).getValue().getParty().getAddress()).isNull();
        assertThat(caseData.getRespondents1().get(1).getValue().getParty().getAddress()).isNotNull();
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

    private Respondent respondent(LocalDate dateOfBirth, String email) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(dateOfBirth)
                .build())
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .lastName("Smith")
                .email(email)
                .build())
            .build();
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
