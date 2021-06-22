package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ReviewDecision;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3170 {
        String familyManNumber = "WR21C50006";
        String migrationId = "FPLA-3170";

        @Test
        void shouldRemoveReviewDecisionFieldsFromCaseData() {
            CaseDetails caseDetails = caseDetailsWithReviewDecisionFields(familyManNumber, migrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getReviewCMODecision()).isNull();
            assertThat(extractedCaseData.getReviewDraftOrdersData().getReviewDecision1()).isNull();
        }

        @Test
        void shouldMigrateWhenCaseDoesNotHaveReviewDecisionFields() {
            CaseDetails caseDetails = caseDetailsWithoutReviewDecisionFields(familyManNumber, migrationId);
            Assertions.assertDoesNotThrow(() -> postAboutToSubmitEvent(caseDetails));
        }

        @Test
        void shouldThrowExceptionForIncorrectFamilyManNumber() {
            String incorrectFamilyManNum = "12456";
            CaseDetails caseDetails = caseDetailsWithReviewDecisionFields(incorrectFamilyManNum, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3170: Expected family man case number to be %s but was %s",
                    familyManNumber, incorrectFamilyManNum));
        }

        @Test
        void shouldNotRemoveAdditionalApplicationBundleForIncorrectMigrationId() {
            String incorrectMigrationId = "incorrect migration id";
            CaseDetails caseDetails = caseDetailsWithReviewDecisionFields(familyManNumber, incorrectMigrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getReviewCMODecision()).isNotNull();
            assertThat(extractedCaseData.getReviewDraftOrdersData().getReviewDecision1()).isNotNull();
        }

        private CaseDetails caseDetailsWithReviewDecisionFields(String familyManNumber, String migrationId) {

            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .reviewCMODecision(ReviewDecision.builder().build())
                .reviewDraftOrdersData(ReviewDraftOrdersData.builder()
                    .reviewDecision1(ReviewDecision.builder().build()).build())
                .familyManCaseNumber(familyManNumber)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private CaseDetails caseDetailsWithoutReviewDecisionFields(String familyManNumber, String migrationId) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

    }

}
