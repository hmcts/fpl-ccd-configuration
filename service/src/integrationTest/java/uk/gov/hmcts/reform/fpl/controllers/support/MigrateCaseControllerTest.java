package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.State.DELETED;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3294 {
        final String migrationId = "FPLA-3294";

        @Test
        void shouldRemoveAllDataAndMoveStateToDeletedForFirstCase() {
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("CLOSED")
                .data(Map.of(
                    "familyManCaseNumber", "SA21C50089",
                    "name", "Test",
                    "migrationId", migrationId))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

            assertThat(response.getData()).isEmpty();
            assertThat(response.getState()).isEqualTo(DELETED.getValue());
        }

        @Test
        void shouldRemoveAllDataAndMoveStateToDeletedForSecondCase() {
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("CLOSED")
                .data(Map.of(
                    "familyManCaseNumber", "SA21C50091",
                    "name", "Test",
                    "migrationId", migrationId))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

            assertThat(response.getData()).isEmpty();
            assertThat(response.getState()).isEqualTo(DELETED.getValue());
        }

        @Test
        void shouldThrowErrorWhenUnexpectedFamilyManNumber() {
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("Submitted")
                .data(Map.of(
                    "familyManCaseNumber", "123",
                    "name", "Test",
                    "migrationId", migrationId))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .hasMessageContaining("Migration FPLA-3294: Family man number 123 was not expected");
        }
    }
}
