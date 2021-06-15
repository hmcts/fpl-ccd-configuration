package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3135 {
        String familyManNumber = "DE21C50016";
        String migrationId = "FPLA-3135";
        UUID documentToRemoveUUID = UUID.fromString("2acc1f5f-ff76-4c3e-b3fc-087ebebd2911");
        UUID incorrectDocument1UUID = UUID.randomUUID();
        UUID incorrectDocument2UUID = UUID.randomUUID();

        DocumentReference documentToRemove = testDocumentReference("Correct court admin document");
        DocumentReference incorrectDocument1 = testDocumentReference("Incorrect court admin document1");
        DocumentReference incorrectDocument2 = testDocumentReference("Incorrect court admin document2");

        @Test
        void shouldRemoveDocumentFromOtherCourtAdminDocumentsCollection() {
            List<Element<CourtAdminDocument>> otherCourtAdminDocuments = List.of(
                element(incorrectDocument1UUID, CourtAdminDocument.builder()
                    .documentTitle("incorrect document1").document(incorrectDocument1).build()),
                element(documentToRemoveUUID, CourtAdminDocument.builder()
                    .documentTitle("correct document to remove").document(documentToRemove).build()),
                element(incorrectDocument2UUID, CourtAdminDocument.builder()
                    .documentTitle("incorrect document2").document(incorrectDocument2).build())
            );

            CaseDetails caseDetails = caseDetails(otherCourtAdminDocuments, migrationId);
            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

            List<Element<CourtAdminDocument>> updatedDocuments
                = extractCaseData(response).getOtherCourtAdminDocuments();

            assertThat(updatedDocuments).doesNotContain(otherCourtAdminDocuments.get(1));
            assertThat(updatedDocuments).containsExactly(
                otherCourtAdminDocuments.get(0), otherCourtAdminDocuments.get(2));

            assertThat((String) response.getData().get("documentViewLA"))
                .doesNotContain(otherCourtAdminDocuments.get(1).getValue().getDocumentTitle());
            assertThat((String) response.getData().get("documentViewHMCTS"))
                .doesNotContain(otherCourtAdminDocuments.get(1).getValue().getDocumentTitle());
            assertThat((String) response.getData().get("documentViewNC"))
                .doesNotContain(otherCourtAdminDocuments.get(1).getValue().getDocumentTitle());
            assertThat((String) response.getData().get("migrationId")).isNull();
        }

        @Test
        void shouldNotUpdateCourtAdminDocumentsCollectionWhenExpectedDocumentIdDoesNotExist() {
            List<Element<CourtAdminDocument>> otherCourtAdminDocuments = List.of(
                element(incorrectDocument1UUID, CourtAdminDocument.builder()
                    .documentTitle("incorrect document1").document(incorrectDocument1).build()),
                element(incorrectDocument2UUID, CourtAdminDocument.builder()
                    .documentTitle("incorrect document2").document(incorrectDocument2).build())
            );

            CaseDetails caseDetails = caseDetails(otherCourtAdminDocuments, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format("Migration failed on case %s: Expected %s but not found",
                    familyManNumber, documentToRemoveUUID));
        }

        @Test
        void shouldNotMigrateCaseForIncorrectMigrationId() {
            List<Element<CourtAdminDocument>> otherCourtAdminDocuments = List.of(
                element(incorrectDocument1UUID, CourtAdminDocument.builder()
                    .documentTitle("incorrect document1").document(incorrectDocument1).build()),
                element(documentToRemoveUUID, CourtAdminDocument.builder()
                    .documentTitle("correct document to remove").document(documentToRemove).build())
            );

            String incorrectMigrationId = "FPLA-3030";
            CaseDetails caseDetails = caseDetails(otherCourtAdminDocuments, incorrectMigrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOtherCourtAdminDocuments()).isEqualTo(otherCourtAdminDocuments);
        }

        @Test
        void shouldNotMigrateCaseForIncorrectFamilyManNumber() {
            List<Element<CourtAdminDocument>> otherCourtAdminDocuments = List.of(
                element(incorrectDocument1UUID, CourtAdminDocument.builder()
                    .documentTitle("incorrect document1").document(incorrectDocument1).build()),
                element(documentToRemoveUUID, CourtAdminDocument.builder()
                    .documentTitle("correct document to remove").document(documentToRemove).build())
            );

            String incorrectFamilyManId = "SA21C52424";
            CaseDetails caseDetails = caseDetails(otherCourtAdminDocuments, incorrectFamilyManId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOtherCourtAdminDocuments()).isEqualTo(otherCourtAdminDocuments);
        }

        private CaseDetails caseDetails(List<Element<CourtAdminDocument>> otherCourtAdminDocuments,
                                        String migrationId) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .otherCourtAdminDocuments(otherCourtAdminDocuments)
                .familyManCaseNumber(familyManNumber)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

}
