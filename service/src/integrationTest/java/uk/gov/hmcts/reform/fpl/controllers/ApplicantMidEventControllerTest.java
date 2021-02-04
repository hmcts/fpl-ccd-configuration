package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(ApplicantController.class)
@OverrideAutoConfiguration(enabled = true)
class ApplicantMidEventControllerTest extends AbstractControllerTest {
    private static final String ERROR_MESSAGE = "Payment by account (PBA) number must include 7 numbers";

    ApplicantMidEventControllerTest() {
        super("enter-applicant");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "pba1234567", "PBA1234567"})
    void shouldReturnNoErrorsWhenValidPbaNumber(String input) {
        CaseDetails caseDetails = getCaseDetails(input);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);

        CaseData caseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);

        assertThat(caseData.getApplicants().get(0).getValue().getParty().getPbaNumber()).isEqualTo("PBA1234567");
    }

    @ParameterizedTest
    @ValueSource(strings = {"  ", "\t", "\n", "123", "12345678"})
    void shouldReturnErrorsWhenThereIsInvalidPbaNumber(String input) {
        CaseDetails caseDetails = getCaseDetails(input);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).contains(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnNoErrorsWhenThereIsNewApplicantAndPbaNumberIsNull() {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(Map.of("applicants", ImmutableList.of(Element.builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .email(EmailAddress.builder().build())
                        .build())
                    .build())
                .build()),
                "solicitor", Solicitor.builder().build()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    @Test
    void shouldReturnErrorsWhenApplicantEmailsAreInValid() {
        CaseData caseData = CaseData.builder()
            .applicants(List.of(element(buildApplicant("email@example.com")),
                element(buildApplicant("<John Doe> johndoe@email.com")),
                element(buildApplicant("email@example.com")),
                element(buildApplicant("very.unusual.”@”.unusual.com@example.com"))))
            .solicitor(Solicitor.builder().build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).contains(
            "Applicant 2: Enter an email address in the correct format, for example name@example.com",
            "Applicant 4: Enter an email address in the correct format, for example name@example.com");
    }

    @Test
    void shouldNotReturnErrorsWhenApplicantEmailsAreValid() {
        CaseData caseData = CaseData.builder()
            .applicants(List.of(element(buildApplicant("email@example.com")),
                element(buildApplicant("email@example.com"))))
            .solicitor(Solicitor.builder().build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenSolicitorEmailIsValid() {
        CaseData caseData = CaseData.builder()
            .applicants(List.of(element(buildApplicant("email@example.com")),
                element(buildApplicant("email@example.com"))))
            .solicitor(Solicitor.builder()
                .email("email@example.com")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenSolicitorEmailIsInValid() {
        CaseData caseData = CaseData.builder()
            .applicants(List.of(element(buildApplicant("email@example.com")),
                element(buildApplicant("email@example.com"))))
            .solicitor(Solicitor.builder()
                .email("<John Doe> johndoe@email.com")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).contains(
            "Solicitor: Enter an email address in the correct format, for example name@example.com");
    }

    @Test
    void shouldReturnErrorsWhenBothSolicitorAndApplicantEmailIsInValid() {
        CaseData caseData = CaseData.builder()
            .applicants(List.of(element(buildApplicant("<John Doe> johndoe@email.com")),
                element(buildApplicant("email@example.com"))))
            .solicitor(Solicitor.builder()
                .email("<John Doe> johndoe@email.com")
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(asCaseDetails(caseData));

        assertThat(callbackResponse.getErrors()).contains(
            "Applicant 1: Enter an email address in the correct format, for example name@example.com",
            "Solicitor: Enter an email address in the correct format, for example name@example.com");
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

    private CaseDetails getCaseDetails(String pbaNumber) {
        return CaseDetails.builder()
            .id(12345L)
            .data(ImmutableMap.of("applicants", ImmutableList.of(Element.builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .email(EmailAddress.builder().build())
                        .pbaNumber(pbaNumber)
                        .build())
                    .build())
                .build()),
                "solicitor", Solicitor.builder().build()))
            .build();
    }
}
