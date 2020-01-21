package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAscii;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
public class AddCaseNumberControllerTest extends AbstractControllerTest {
    private static final String FAMILY_MAN_CASE_NUMBER_KEY = "familyManCaseNumber";

    AddCaseNumberControllerTest() {
        super("add-case-number");
    }

    @Test
    void aboutToSubmitShouldReturnErrorWhenFamilymanCaseNumberNotAlphanumeric() {
        CallbackRequest callbackRequest = buildCallbackRequest(randomAscii(9) + "+");

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest.getCaseDetails(),
            SC_OK);

        assertThat(callbackResponse.getErrors()).containsExactly("Enter a valid FamilyMan case number");
    }

    @Test
    void aboutToSubmitShouldNotReturnErrorWhenFamilymanCaseNumberAlphanumeric() {
        final String expectedFamilymanCaseNumber = randomAlphabetic(10);
        CallbackRequest callbackRequest = buildCallbackRequest(expectedFamilymanCaseNumber);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(callbackRequest.getCaseDetails(),
            SC_OK);

        assertThat(callbackResponse.getErrors()).isEmpty();
        assertThat(callbackResponse.getData()).containsKey(FAMILY_MAN_CASE_NUMBER_KEY);
        assertThat(callbackResponse.getData().get(FAMILY_MAN_CASE_NUMBER_KEY)).isEqualTo(expectedFamilymanCaseNumber);
    }

    private CallbackRequest buildCallbackRequest(final String familyManCaseNumber) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .data(ImmutableMap.of(FAMILY_MAN_CASE_NUMBER_KEY, familyManCaseNumber))
                .build())
            .build();
    }
}
