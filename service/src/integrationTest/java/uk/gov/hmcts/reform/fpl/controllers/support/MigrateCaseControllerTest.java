package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
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
    class Fpla3092 {
        String familyManNumber = "CF20C50063";
        String migrationId = "FPLA-3092";
        UUID documentToRemoveUUID = UUID.fromString("a1e1f56d-18b8-4123-acaf-7c276627628e");
        UUID incorrectDocumentUUID1 = UUID.randomUUID();
        UUID incorrectDocumentUUID2 = UUID.randomUUID();

        DocumentReference documentToRemove = testDocumentReference("Correspondence document to remove");
        DocumentReference incorrectDocument1 = testDocumentReference("Incorrect correspondence document");
        DocumentReference incorrectDocument2 = testDocumentReference("Incorrect correspondence document2");

        @Test
        void shouldRemoveCorrespondenceDocument() {
            List<Element<SupportingEvidenceBundle>> correspondenceDocuments = List.of(
                element(documentToRemoveUUID, SupportingEvidenceBundle.builder().document(documentToRemove).build()),
                element(
                    incorrectDocumentUUID1, SupportingEvidenceBundle.builder().document(incorrectDocument1).build())
            );

            CaseDetails caseDetails = caseDetails(correspondenceDocuments, familyManNumber, migrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getCorrespondenceDocuments())
                .isEqualTo(List.of(correspondenceDocuments.get(1)));
        }

        @Test
        void shouldThrowExceptionWhenDocumentUuidISNotFound() {
            List<Element<SupportingEvidenceBundle>> correspondenceDocuments = List.of(element(incorrectDocumentUUID1,
                SupportingEvidenceBundle.builder().document(incorrectDocument1).build()),
                element(incorrectDocumentUUID2,
                    SupportingEvidenceBundle.builder().document(incorrectDocument2).build())
            );

            CaseDetails caseDetails = caseDetails(correspondenceDocuments, familyManNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration failed on case %s: Expected correspondence document id %s but not found",
                    familyManNumber, documentToRemoveUUID));
        }

        @Test
        void shouldThrowExceptionForIncorrectFamilyManNumber() {
            String incorrectFamilyManId = "INCORRECT_FAMILY_MAN_ID";

            List<Element<SupportingEvidenceBundle>> correspondenceDocuments = List.of(
                element(documentToRemoveUUID, SupportingEvidenceBundle.builder().document(incorrectDocument1).build())
            );

            CaseDetails caseDetails = caseDetails(correspondenceDocuments, incorrectFamilyManId, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3092: Expected family man case number to be %s but was %s",
                    familyManNumber, incorrectFamilyManId));
        }

        @Test
        void shouldNotRemoveCorrespondenceDocumentForIncorrectMigrationId() {
            List<Element<SupportingEvidenceBundle>> correspondenceDocuments = List.of(
                element(documentToRemoveUUID, SupportingEvidenceBundle.builder().document(documentToRemove).build())
            );

            String incorrectMigrationId = "some migration id";
            CaseDetails caseDetails = caseDetails(correspondenceDocuments, familyManNumber, incorrectMigrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getCorrespondenceDocuments()).isEqualTo(correspondenceDocuments);
        }

        private CaseDetails caseDetails(List<Element<SupportingEvidenceBundle>> correspondenceDocuments,
                                        String familyManId,
                                        String migrationId) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .correspondenceDocuments(correspondenceDocuments)
                .familyManCaseNumber(familyManId)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

}
