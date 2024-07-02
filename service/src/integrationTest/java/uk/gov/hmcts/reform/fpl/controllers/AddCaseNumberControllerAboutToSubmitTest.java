package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
class AddCaseNumberControllerAboutToSubmitTest extends AbstractCallbackTest {

    AddCaseNumberControllerAboutToSubmitTest() {
        super("add-case-number");
    }

    @WithMockUser
    @Test
    void aboutToSubmitShouldReturnErrorWhenFamilyManCaseNumberNotAlphanumeric() {
        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("NOT ALPHANUMERIC")
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        assertThat(callbackResponse.getErrors()).containsExactly("Enter a valid FamilyMan case number");
    }

    @WithMockUser
    @Test
    void aboutToSubmitShouldNotReturnErrorWhenFamilyManCaseNumberAlphanumeric() {
        final String expectedFamilyManCaseNumber = "ALPHANUM3RIC";

        CaseData caseData = CaseData.builder()
            .familyManCaseNumber(expectedFamilyManCaseNumber)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        assertThat(callbackResponse.getErrors()).isEmpty();
        assertThat(callbackResponse.getData().get("familyManCaseNumber")).isEqualTo(expectedFamilyManCaseNumber);
    }

}
