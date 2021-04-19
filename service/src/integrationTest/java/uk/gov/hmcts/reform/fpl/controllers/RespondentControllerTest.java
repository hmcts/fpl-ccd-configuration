package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.UNREGISTERED_RESPONDENT_SOLICICTOR_TEMPLATE;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.AssertionHelper.checkUntil;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.callbackRequest;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerTest extends AbstractCallbackTest {

    private static final String SOLICITOR_ORG_ID = "Organisation ID";
    private static final String SOLICITOR_EMAIL = "solicitor@email.com";

    RespondentControllerTest() {
        super("enter-respondents");
    }

    private static final String DOB_ERROR = "Date of birth cannot be in the future";
    private static final String MAX_RESPONDENTS_ERROR = "Maximum number of respondents is 10";
    private static final String CASE_ID = "12345";
    private static final String NOTIFICATION_REFERENCE = "localhost/" + CASE_ID;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private NotificationClient notificationClient;

    @BeforeEach
    void setUp() {
        when(featureToggleService.hasRSOCaseAccess()).thenReturn(false);
    }

    @Test
    void aboutToStartShouldPrepopulateRespondent() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("data", "some data"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("respondents1");
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
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).containsExactly(DOB_ERROR);
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
    void shouldReturnEmailAddressErrorsForRespondentSolicitorEmailWhenInvalid() {
        CaseData caseData = CaseData.builder()
            .respondents1(wrapElements(respondent(dateNow().plusDays(1), "Test User <e.test@test.com>")))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(DOB_ERROR,
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
    void shouldReturnRespondentRemovedValidationErrorsWhenRespondentRemoved() {
        when(featureToggleService.hasRSOCaseAccess()).thenReturn(true);
        CaseData caseData = CaseData.builder()
            .respondents1(List.of())
            .state(SUBMITTED)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(asCaseDetails(caseData))
            .caseDetailsBefore(asCaseDetails(CaseData.builder()
                .respondents1(List.of(element(respondent(dateNow(), "test@test.com"))))
                .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(callbackRequest);

        assertThat(callbackResponse.getErrors()).isEqualTo(List.of("Removing an existing respondent is not allowed"));
    }

    @Test
    void shouldGenerateRespondentPoliciesWhenToggleOnAndStateIsNotOpen() {
        when(featureToggleService.hasRSOCaseAccess()).thenReturn(true);

        Respondent respondentWithRepresentative = respondent(dateNow()).toBuilder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .organisation(Organisation.builder()
                    .organisationID(SOLICITOR_ORG_ID)
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .state(SUBMITTED)
            .applicants(wrapElements(Applicant.builder()
                .party(ApplicantParty.builder()
                    .organisationName("Applicant org name").build())
                .build()))
            .respondents1(wrapElements(respondentWithRepresentative))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        CaseData responseData = extractCaseData(callbackResponse);

        assertThat(responseData.getRespondentPolicyData().getRespondentPolicy0().getOrganisation().getOrganisationID())
            .isEqualTo(SOLICITOR_ORG_ID);
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

    @Test
    void shouldPublishRespondentsUpdatedEvent() {
        Respondent respondentWithRepresentative = respondent(dateNow()).toBuilder()
            .legalRepresentation(YES.getValue())
            .solicitor(RespondentSolicitor.builder()
                .email(SOLICITOR_EMAIL)
                .unregisteredOrganisation(UnregisteredOrganisation.builder()
                    .name("Unregistered Org")
                    .build())
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_ID))
            .respondents1(wrapElements(respondentWithRepresentative))
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .build();

        postSubmittedEvent(caseData);

        checkUntil(() -> verify(notificationClient).sendEmail(
            eq(UNREGISTERED_RESPONDENT_SOLICICTOR_TEMPLATE),
            eq(SOLICITOR_EMAIL),
            anyMap(),
            eq(NOTIFICATION_REFERENCE)
        ));
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
