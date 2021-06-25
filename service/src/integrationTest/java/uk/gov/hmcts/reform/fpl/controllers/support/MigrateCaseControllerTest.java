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
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
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
    class Fpla3175 {
        String familyManNumber = "CV21C50026";
        String migrationId = "FPLA-3175";

        final UUID documentId1 = UUID.fromString("339ad24a-e83c-4b81-9743-46c7771b3975");
        final UUID documentId2 = UUID.fromString("dc8ad08b-e7ba-4075-a9e4-0ff8fc85616b");
        final UUID documentId3 = UUID.fromString("008f3053-d949-4aaa-9d65-1027e88ed288");
        final UUID documentId4 = UUID.fromString("671d6805-c042-4b3e-88e8-7c7fd103a026");
        final UUID documentId5 = UUID.fromString("74c918f2-6409-4651-bb7b-1f58c39aee94");
        final UUID documentId6 = UUID.fromString("03343373-df27-4744-a63c-56a98442662a");

        final String documentTitle1 = "Children's Guardian Position Statement 22 .06.2021";
        final String documentTitle2 = "Supporting documents for Children's Guardian Position Statement no 1";
        final String documentTitle3 = "Supporting documents for Children's Guardian Position Statement no 2";
        final String documentTitle4 = "Supporting documents for Children's Guardian Position Statement no 3";
        final String documentTitle5 = "Supporting documents for Children's Guardian Position Statement no 4";
        final String documentTitle6 = "Supporting documents for Children's Guardian Position Statement no 5";

        final DocumentReference document = testDocumentReference();

        @Test
        void shouldRemoveOtherCourtAdminDocuments() {
            List<Element<CourtAdminDocument>> otherCourtAdminDocuments = buildExpectedOtherCourtAdminDocuments();

            CaseDetails caseDetails = caseDetails(otherCourtAdminDocuments, familyManNumber, migrationId);

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

            assertThat(extractCaseData(response).getOtherCourtAdminDocuments())
                .isEqualTo(List.of(otherCourtAdminDocuments.get(0)));

            assertThat((String) response.getData().get("documentViewLA")).doesNotContain(
                documentTitle1, documentTitle2, documentTitle3, documentTitle4, documentTitle5, documentTitle6);
            assertThat((String) response.getData().get("documentViewHMCTS")).doesNotContain(
                documentTitle1, documentTitle2, documentTitle3, documentTitle4, documentTitle5, documentTitle6);
            assertThat((String) response.getData().get("documentViewNC")).doesNotContain(
                documentTitle1, documentTitle2, documentTitle3, documentTitle4, documentTitle5, documentTitle6);
        }

        @Test
        void shouldThrowExceptionWhenOtherCourtAdminDocumentIdDoesNotMatch() {
            DocumentReference document = testDocumentReference();
            List<Element<CourtAdminDocument>> otherCourtAdminDocuments = List.of(
                element(documentId1,
                    CourtAdminDocument.builder().documentTitle(documentTitle1).document(document).build()),
                element(UUID.randomUUID(),
                    CourtAdminDocument.builder().documentTitle(documentTitle2).document(document).build()),
                element(documentId3,
                    CourtAdminDocument.builder().documentTitle(documentTitle3).document(document).build()),
                element(documentId4,
                    CourtAdminDocument.builder().documentTitle(documentTitle4).document(document).build()),
                element(documentId5,
                    CourtAdminDocument.builder().documentTitle(documentTitle5).document(document).build()),
                element(documentId6,
                    CourtAdminDocument.builder().documentTitle(documentTitle6).document(document).build()));

            CaseDetails caseDetails = caseDetails(otherCourtAdminDocuments, familyManNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3175: Expected other court admin document Id %s and document title '%s' "
                        + "but not found", documentId2, documentTitle2));
        }

        @Test
        void shouldThrowExceptionWhenOtherCourtAdminDocumentTitleDoesNotMatch() {
            DocumentReference document = testDocumentReference();
            List<Element<CourtAdminDocument>> otherCourtAdminDocuments = List.of(
                element(documentId1,
                    CourtAdminDocument.builder().documentTitle(documentTitle1).document(document).build()),
                element(documentId2,
                    CourtAdminDocument.builder().documentTitle(documentTitle2).document(document).build()),
                element(documentId3,
                    CourtAdminDocument.builder().documentTitle("incorrect title").document(document).build()),
                element(documentId4,
                    CourtAdminDocument.builder().documentTitle(documentTitle4).document(document).build()),
                element(documentId5,
                    CourtAdminDocument.builder().documentTitle(documentTitle5).document(document).build()),
                element(documentId6,
                    CourtAdminDocument.builder().documentTitle(documentTitle6).document(document).build()));

            CaseDetails caseDetails = caseDetails(otherCourtAdminDocuments, familyManNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3175: Expected other court admin document Id %s and document title '%s' "
                        + "but not found", documentId3, documentTitle3));
        }

        @Test
        void shouldThrowExceptionWhenAllExpectedOtherCourtDocumentsDoNotExist() {
            DocumentReference document = testDocumentReference();
            // documentId6 is missing
            List<Element<CourtAdminDocument>> otherCourtAdminDocuments = List.of(
                element(documentId1,
                    CourtAdminDocument.builder().documentTitle(documentTitle1).document(document).build()),
                element(documentId2,
                    CourtAdminDocument.builder().documentTitle(documentTitle2).document(document).build()),
                element(documentId3,
                    CourtAdminDocument.builder().documentTitle(documentTitle3).document(document).build()),
                element(documentId4,
                    CourtAdminDocument.builder().documentTitle(documentTitle4).document(document).build()),
                element(documentId5,
                    CourtAdminDocument.builder().documentTitle(documentTitle5).document(document).build()));

            CaseDetails caseDetails = caseDetails(otherCourtAdminDocuments, familyManNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3175: Expected other court admin document Id %s and document title '%s' "
                        + "but not found", documentId6, documentTitle6));
        }

        @Test
        void shouldThrowExceptionForIncorrectFamilyManNumber() {
            List<Element<CourtAdminDocument>> otherCourtAdminDocuments = buildExpectedOtherCourtAdminDocuments();

            String incorrectFamilyManId = "12456";
            CaseDetails caseDetails = caseDetails(otherCourtAdminDocuments, incorrectFamilyManId, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3175: Expected family man case number to be %s but was %s",
                    familyManNumber, incorrectFamilyManId));
        }

        @Test
        void shouldNotMigrateCaseForIncorrectMigrationId() {
            List<Element<CourtAdminDocument>> otherCourtAdminDocuments = buildExpectedOtherCourtAdminDocuments();

            String incorrectMigrationId = "incorrect migration id";
            CaseDetails caseDetails = caseDetails(otherCourtAdminDocuments, familyManNumber, incorrectMigrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getOtherCourtAdminDocuments()).isEqualTo(otherCourtAdminDocuments);
        }

        private List<Element<CourtAdminDocument>> buildExpectedOtherCourtAdminDocuments() {
            return List.of(
                element(UUID.randomUUID(),
                    CourtAdminDocument.builder().documentTitle("document1").document(document).build()),
                element(documentId1,
                    CourtAdminDocument.builder().documentTitle(documentTitle1).document(document).build()),
                element(documentId2,
                    CourtAdminDocument.builder().documentTitle(documentTitle2).document(document).build()),
                element(documentId3,
                    CourtAdminDocument.builder().documentTitle(documentTitle3).document(document).build()),
                element(documentId4,
                    CourtAdminDocument.builder().documentTitle(documentTitle4).document(document).build()),
                element(documentId5,
                    CourtAdminDocument.builder().documentTitle(documentTitle5).document(document).build()),
                element(documentId6,
                    CourtAdminDocument.builder().documentTitle(documentTitle6).document(document).build()));
        }

        private CaseDetails caseDetails(List<Element<CourtAdminDocument>> otherCourtAdminDocuments,
                                        String familyManCaseNumber,
                                        String migrationId) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .state(State.CASE_MANAGEMENT)
                .otherCourtAdminDocuments(otherCourtAdminDocuments)
                .familyManCaseNumber(familyManCaseNumber)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3093 {
        String familyManNumber = "KH21C50008";
        String migrationId = "FPLA-3093";
        final UUID additionalApplicationBundleId = UUID.fromString("c7b47c00-4b7a-4dd8-8bce-140e41ab4bb0");
        final UUID c2ApplicationId = UUID.fromString("30d385b9-bdc5-4145-aeb5-ffee5afd1f02");
        UUID anotherBundleId = UUID.randomUUID();
        UUID anotherC2ApplicationId = UUID.randomUUID();

        @Test
        void shouldRemoveAdditionalApplicationBundle() {
            List<Element<AdditionalApplicationsBundle>> bundles = buildAdditionalApplicationsBundles(
                additionalApplicationBundleId, c2ApplicationId);

            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, migrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getAdditionalApplicationsBundle()).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenAdditionalApplicationBundleIdIsNotFound() {
            List<Element<AdditionalApplicationsBundle>> bundles = buildAdditionalApplicationsBundles(
                anotherBundleId, c2ApplicationId);

            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3093: Expected additional application bundle id to be %s but not found",
                    additionalApplicationBundleId));
        }

        @Test
        void shouldThrowExceptionWhenC2ApplicationIdIsNotFound() {
            List<Element<AdditionalApplicationsBundle>> bundles = buildAdditionalApplicationsBundles(
                additionalApplicationBundleId, anotherC2ApplicationId);

            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3093: Expected c2 bundle Id to be %s but not found", c2ApplicationId));
        }

        @Test
        void shouldThrowExceptionForIncorrectFamilyManNumber() {
            List<Element<AdditionalApplicationsBundle>> bundles = buildAdditionalApplicationsBundles(
                anotherBundleId, anotherC2ApplicationId);

            String incorrectFamilyManNum = "12456";
            CaseDetails caseDetails = caseDetails(bundles, incorrectFamilyManNum, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3093: Expected family man case number to be %s but was %s",
                    familyManNumber, incorrectFamilyManNum));
        }

        @Test
        void shouldNotRemoveAdditionalApplicationBundleForIncorrectMigrationId() {
            List<Element<AdditionalApplicationsBundle>> bundles = buildAdditionalApplicationsBundles(
                additionalApplicationBundleId, c2ApplicationId);

            String incorrectMigrationId = "incorrect migration id";
            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, incorrectMigrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getAdditionalApplicationsBundle()).isEqualTo(bundles);
        }

        private CaseDetails caseDetails(List<Element<AdditionalApplicationsBundle>> additionalApplications,
                                        String familyManCaseNumber,
                                        String migrationId) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .state(State.CASE_MANAGEMENT)
                .additionalApplicationsBundle(additionalApplications)
                .familyManCaseNumber(familyManCaseNumber)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private List<Element<AdditionalApplicationsBundle>> buildAdditionalApplicationsBundles(
            UUID additionalApplicationBundleId,
            UUID c2ApplicationId) {
            return List.of(element(additionalApplicationBundleId,
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(c2ApplicationId).build()).build()));
        }

    }
}
