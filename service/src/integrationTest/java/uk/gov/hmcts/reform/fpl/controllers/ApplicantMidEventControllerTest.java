package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.service.ApplicantService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.PbaNumberService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
@Import({PbaNumberService.class, ValidateEmailService.class})
class ApplicantMidEventControllerTest extends AbstractCallbackTest {
    private static final String ERROR_MESSAGE = "Payment by account (PBA) number must include 7 numbers";

    @MockBean
    private ApplicantService applicantService;

    @MockBean
    private OrganisationService organisationService;

    ApplicantMidEventControllerTest() {
        super("enter-applicant");
    }

    @WithMockUser
    @ParameterizedTest
    @ValueSource(strings = {"1234567", "pba1234567", "PBA1234567"})
    void shouldReturnNoErrorsWhenValidPbaNumber(String input) {
        CaseData caseData = getCaseData(input);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).isNull();

        caseData = extractCaseData(callbackResponse);

        assertThat(caseData.getApplicants().get(0).getValue().getParty().getPbaNumber()).isEqualTo("PBA1234567");
    }

    @WithMockUser
    @ParameterizedTest
    @ValueSource(strings = {"  ", "\t", "\n", "123", "12345678"})
    void shouldReturnErrorsWhenThereIsInvalidPbaNumber(String input) {
        CaseData caseData = getCaseData(input);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @WithMockUser
    @Test
    void shouldReturnNoErrorsWhenThereIsNewApplicantAndPbaNumberIsNull() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(wrapElements(Applicant.builder()
                .party(ApplicantParty.builder()
                    .email(EmailAddress.builder().build())
                    .build()).build()))
            .solicitor(Solicitor.builder()
                .email("email@example.com")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getErrors()).isNull();
    }

    @WithMockUser
    @Test
    void shouldReturnErrorsWhenApplicantAndSolicitorEmailsAreInvalid() {
        CaseData caseData = CaseData.builder()
            .applicants(wrapElements(buildApplicant("email@example.com"),
                buildApplicant("<John Doe> johndoe@email.com"),
                buildApplicant("email@example.com"),
                buildApplicant("very.unusual.”@”.unusual.com@example.com")))
            .solicitor(Solicitor.builder()
                .email("<John Doe> johndoe@email.com")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).containsExactly(
            "Applicant 2: Enter an email address in the correct format, for example name@example.com",
            "Applicant 4: Enter an email address in the correct format, for example name@example.com",
            "Solicitor: Enter an email address in the correct format, for example name@example.com");
    }

    @WithMockUser
    @Test
    void shouldNotReturnErrorsWhenApplicantAndSolicitorEmailsAreValid() {
        CaseData caseData = CaseData.builder()
            .applicants(wrapElements(buildApplicant("email@example.com"),
                buildApplicant("email@example.com")))
            .solicitor(Solicitor.builder()
                .email("email@example.com")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).isNull();
    }

    @WithMockUser
    @Test
    void shouldReturnErrorsWhenSolicitorEmailsIsNull() {
        CaseData caseData = CaseData.builder()
            .applicants(wrapElements(buildApplicant("email@example.com"),
                buildApplicant("email@example.com")))
            .solicitor(Solicitor.builder()
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).containsExactly(
            "Solicitor: Enter an email address in the correct format, for example name@example.com"
        );
    }

    private Applicant buildApplicant(String email) {
        return Applicant.builder()
            .party(ApplicantParty.builder()
                .email(EmailAddress.builder()
                    .email(email)
                    .build())
                .build())
            .build();
    }

    private CaseData getCaseData(String pbaNumber) {
        return CaseData.builder()
            .id(12345L)
            .applicants(wrapElements(Applicant.builder()
                .party(ApplicantParty.builder()
                    .email(EmailAddress.builder().build())
                    .pbaNumber(pbaNumber)
                    .build()).build()))
            .solicitor(Solicitor.builder()
                .email("email@example.com")
                .build())
            .build();
    }
}
