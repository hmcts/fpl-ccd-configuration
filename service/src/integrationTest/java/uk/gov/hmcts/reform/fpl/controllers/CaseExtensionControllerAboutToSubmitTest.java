package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseExtensionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseExtensionControllerAboutToSubmitTest extends AbstractControllerTest {

    CaseExtensionControllerAboutToSubmitTest() {
        super("case-extension");
    }

    @Autowired
    CaseExtensionController caseExtensionController;

    @Test
    void shouldPopulateCaseCompletionDateWhenSubmittingWithOtherExtensionDate() {
        LocalDate extensionDateOther = LocalDate.of(2030, 11, 12);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("extensionDateOther", extensionDateOther,
                "caseExtensionTimeList", "otherExtension"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData().get("caseCompletionDate")).isEqualTo("2030-11-12");
    }

    @Test
    void shouldPopulateCaseCompletionDateWhenSubmittingWith8WeekExtensionOther() {
        LocalDate eightWeeksExtensionDateOther = LocalDate.of(2030, 11, 12);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseExtensionTimeList", "EightWeekExtension",
                "caseExtensionTimeConfirmationList", "EightWeekExtensionDateOther",
                "eightWeeksExtensionDateOther", eightWeeksExtensionDateOther))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData().get("caseCompletionDate")).isEqualTo("2030-11-12");
    }

    @Test
    void shouldPopulateCaseCompletionDateWhenSubmittingWith8WeekExtension() {
        LocalDate eightWeekExtensionDate = LocalDate.of(2030,11,11);
        setField(caseExtensionController, "eightWeekExtensionDate", eightWeekExtensionDate);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseExtensionTimeList", "EightWeekExtension",
                "caseExtensionTimeConfirmationList", "EightWeekExtension"))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData().get("caseCompletionDate")).isEqualTo("2030-11-11");
    }
}
