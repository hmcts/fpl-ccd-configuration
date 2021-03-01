package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseExtensionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseExtensionControllerMidEventTest extends AbstractControllerTest {

    CaseExtensionControllerMidEventTest() {
        super("case-extension");
    }

    @Autowired
    CaseExtensionController caseExtensionController;

    @Test
    void shouldPopulateExtensionDateLabelWith8WeekExtensionDate() {
        LocalDate caseCompletionDate = LocalDate.of(2030, 11, 12);

        CaseData caseData = CaseData.builder()
            .caseCompletionDate(caseCompletionDate)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);

        assertThat(callbackResponse.getData().get("extensionDateEightWeeks")).isEqualTo("7 January 2031");
    }
}
