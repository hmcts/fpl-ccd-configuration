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
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
                    .party(ApplicantParty.builder().build())
                    .build())
                .build())))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseDetails);

        assertThat(callbackResponse.getErrors()).doesNotContain(ERROR_MESSAGE);
    }

    private CaseDetails getCaseDetails(String pbaNumber) {
        return CaseDetails.builder()
            .id(12345L)
            .data(ImmutableMap.of("applicants", ImmutableList.of(Element.builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .pbaNumber(pbaNumber)
                        .build())
                    .build())
                .build())))
            .build();
    }
}
