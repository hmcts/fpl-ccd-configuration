package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.DraftOrderUrgencyOption;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadAdditionalApplicationsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadAdditionalApplicationsAboutToStartControllerTest extends AbstractCallbackTest {

    private static final String ADMIN_ROLE = "caseworker-publiclaw-courtadmin";

    UploadAdditionalApplicationsAboutToStartControllerTest() {
        super("upload-additional-applications");
    }

    @Test
    void shouldClearDraftOrderUrgencyProperty() {
        CaseData caseData = CaseData.builder()
            .draftOrderUrgency(DraftOrderUrgencyOption.builder().urgency(List.of(YesNo.YES)).build())
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData, ADMIN_ROLE));
        assertThat(updatedCaseData.getDraftOrderUrgency()).isNull();
    }
}
