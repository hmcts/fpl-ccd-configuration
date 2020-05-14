package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.HashMap;
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
    void shouldPopulateCaseCompletionDateWhenSubmittingWithOtherDate() {

        Map<String, Object> data = new HashMap<>();

        data.put("extensionDateOther", LocalDate.of(2030, 11, 12));
        data.put("caseExtensionTimeList", "otherExtension");

        CaseDetails caseDetails = CaseDetails.builder()
            .data(data)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData().get("caseCompletionDate")).isEqualTo("2030-11-12");
    }

    @Test
    void shouldPopulateCaseCompletionDateWhenSubmittingWith8WeekExtensionOther() {

        Map<String, Object> data = new HashMap<>();

        data.put("caseExtensionTimeList", "EightWeekExtension");
        data.put("caseExtensionTimeConfirmationList", "EightWeekExtensionDateOther");
        data.put("eightWeeksExtensionDateOther", LocalDate.of(2030, 11, 12));

        CaseDetails caseDetails = CaseDetails.builder()
            .data(data)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData().get("caseCompletionDate")).isEqualTo("2030-11-12");
    }

    @Test
    void shouldPopulateCaseCompletionDateWhenSubmittingWith8WeekExtension() throws NoSuchFieldException {

        Map<String, Object> data = new HashMap<>();

        data.put("caseExtensionTimeList", "EightWeekExtension");
        data.put("caseExtensionTimeConfirmationList", "EightWeekExtension");

        LocalDate date = LocalDate.of(2030,11,11);

        setField(caseExtensionController, "eightWeekExtensionDate", date);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(data)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData().get("caseCompletionDate")).isEqualTo("2030-11-11");
    }
}
