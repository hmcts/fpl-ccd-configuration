package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.EIGHT_WEEK_EXTENSION;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.OTHER_EXTENSION;

@WebMvcTest(CaseExtensionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseExtensionControllerAboutToSubmitTest extends AbstractCallbackTest {

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
                "caseExtensionTimeList", OTHER_EXTENSION))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData().get("caseCompletionDate")).isEqualTo("2030-11-12");
    }

    @Test
    void shouldPopulateCaseCompletionDateWhenSubmittingWith8WeekExtensionOther() {
        LocalDate eightWeeksExtensionDateOther = LocalDate.of(2030, 11, 12);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseExtensionTimeList", EIGHT_WEEK_EXTENSION,
                "caseExtensionTimeConfirmationList", OTHER_EXTENSION,
                "eightWeeksExtensionDateOther", eightWeeksExtensionDateOther))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData().get("caseCompletionDate")).isEqualTo("2030-11-12");
    }

    @Test
    void shouldPopulateCaseCompletionDateWhenSubmittingWith8WeekExtension() {
        LocalDate dateSubmitted = LocalDate.of(2030, 11, 11);
        LocalDate extendedDate = dateSubmitted.plusWeeks(34);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseExtensionTimeList", EIGHT_WEEK_EXTENSION,
                "caseExtensionTimeConfirmationList", EIGHT_WEEK_EXTENSION,
                "dateSubmitted", dateSubmitted))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseDetails);

        assertThat(callbackResponse.getData().get("caseCompletionDate")).isEqualTo(extendedDate.toString());
    }
}
