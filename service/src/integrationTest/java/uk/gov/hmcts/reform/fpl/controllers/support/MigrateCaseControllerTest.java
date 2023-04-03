package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final String INVALID_MIGRATION_ID = "invalid id";

    @Test
    void shouldThrowExceptionWhenMigrationNotMappedForMigrationID() {
        CaseData caseData = CaseData.builder().build();

        assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, INVALID_MIGRATION_ID)))
            .getRootCause()
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No migration mapped to " + INVALID_MIGRATION_ID);
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl1124Rollback {
        final String migrationId = "DFPL-1124Rollback";
        final Long caseId = 1660300177298257L;

        @Test
        void shouldAddDfjAreaAndCourtFiledWhenCourtPresent() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .state(State.CASE_MANAGEMENT)
                .court(Court.builder().code("11").build())
                .dfjArea("SWANSEA")
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData updatedCaseData = extractCaseData(response);
            assertThat(updatedCaseData.getDfjArea()).isNull();
            assertThat(response.getData().get("swanseaDFJCourt")).isNull();
        }

        @Test
        void shouldNotFailRollbackWhenDfjAreaNotPresent() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .state(State.CASE_MANAGEMENT)
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData updatedCaseData = extractCaseData(response);
            assertThat(updatedCaseData.getDfjArea()).isNull();
        }
    }
}
