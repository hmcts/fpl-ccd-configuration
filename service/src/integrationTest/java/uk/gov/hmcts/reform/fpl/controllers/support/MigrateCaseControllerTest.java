package uk.gov.hmcts.reform.fpl.controllers.support;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }


    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3214 {

        final String migrationId = "FPLA-3214";

        @ParameterizedTest
        @EnumSource(HearingOptions.class)
        void shouldRemoveHearingOptionIfPresent(HearingOptions hearingOptions) {

            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("Submitted")
                .data(Map.of(
                    "name", "Test",
                    "hearingOption", hearingOptions,
                    "migrationId", migrationId))
                .build();

            Map<String, Object> expected = new HashMap<>(caseDetails.getData());
            expected.remove("hearingOption");
            expected.remove("migrationId");

            Map<String, Object> response = postAboutToSubmitEvent(caseDetails).getData();

            assertThat(response).isEqualTo(expected);
        }

        @Test
        void shouldRemoveMigrationIdWhenHearingOptionNotPresent() {
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("Submitted")
                .data(Map.of(
                    "name", "Test",
                    "migrationId", migrationId))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails).getData());
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3126 {

        final String migrationId = "FPLA-3126";

        @Test
        void shouldRemoveDraftCMOIfPresent() {
            UUID id = UUID.randomUUID();
            List<Element<HearingBooking>> cancelledHearings =
                List.of(element(HearingBooking.builder().caseManagementOrderId(id).build()));
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("Submitted")
                .data(Map.of(
                    "name", "Test",
                    "familyManCaseNumber", "NE21C50026",
                    "draftUploadedCMOs", List.of(element(id, HearingOrder.builder().title("remove me").build())),
                    "cancelledHearingDetails", cancelledHearings,
                    "migrationId", migrationId))
                .build();

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            cancelledHearings.get(0).getValue().setCaseManagementOrderId(null);

            assertThat(extractedCaseData.getDraftUploadedCMOs()).isEmpty();
            assertThat(extractedCaseData.getCancelledHearingDetails()).isEqualTo(cancelledHearings);
        }

        @Test
        void shouldRemoveMigrationIdWhenNoDraftCMOs() {
            CaseDetails caseDetails = CaseDetails.builder()
                .id(10L)
                .state("Submitted")
                .data(Map.of(
                    "familyManCaseNumber", "NE21C50026",
                    "name", "Test",
                    "migrationId", migrationId))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails).getData());
        }
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
    class Fpla2486 {
        String migrationId = "FPLA-2486";
        String familyManNumber = "SA123456";
        Long caseReference = 1234567812345678L;

        final DocumentReference document = testDocumentReference();

        @Test
        void shouldSortCorrespondenceDocuments() {
            List<Element<SupportingEvidenceBundle>> correspondenceDocuments = buildCorrespondenceDocuments("HMCTS");
            List<Element<SupportingEvidenceBundle>> correspondenceDocumentsLA = buildCorrespondenceDocuments("LA");

            CaseDetails caseDetails = caseDetails(
                correspondenceDocuments, correspondenceDocumentsLA, familyManNumber, migrationId);
            caseDetails.getData().put("correspondenceDocumentsNC", correspondenceDocuments);

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);
            CaseData caseData = extractCaseData(response);
            assertThat(caseData.getCorrespondenceDocuments())
                .isEqualTo(List.of(
                    correspondenceDocuments.get(2), correspondenceDocuments.get(1), correspondenceDocuments.get(0)));

            assertThat(caseData.getCorrespondenceDocumentsLA())
                .isEqualTo(List.of(
                    correspondenceDocumentsLA.get(2), correspondenceDocumentsLA.get(1),
                    correspondenceDocumentsLA.get(0)));

            List<Element<SupportingEvidenceBundle>> correspondenceDocumentsNC =
                mapper.convertValue(response.getData().get("correspondenceDocumentsNC"), new TypeReference<>() {
                });

            assertThat(correspondenceDocumentsNC).isEqualTo(List.of(
                correspondenceDocuments.get(2), correspondenceDocuments.get(1), correspondenceDocuments.get(0)));
        }

        @Test
        void shouldSortCorrespondenceDocumentsWhenCorrespondenceDocumentsLAAreEmpty() {
            List<Element<SupportingEvidenceBundle>> correspondenceDocuments = buildCorrespondenceDocuments("HMCTS");
            CaseDetails caseDetails = caseDetails(correspondenceDocuments, List.of(), familyManNumber, migrationId);
            caseDetails.getData().put("correspondenceDocumentsNC", correspondenceDocuments);

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);


            assertThat(extractCaseData(response).getCorrespondenceDocuments())
                .isEqualTo(List.of(
                    correspondenceDocuments.get(2), correspondenceDocuments.get(1), correspondenceDocuments.get(0)));
            assertThat(extractCaseData(response).getCorrespondenceDocumentsLA()).isEmpty();

            List<Element<SupportingEvidenceBundle>> correspondenceDocumentsNC =
                mapper.convertValue(response.getData().get("correspondenceDocumentsNC"), new TypeReference<>() {
                });

            assertThat(correspondenceDocumentsNC).isEqualTo(List.of(
                correspondenceDocuments.get(2), correspondenceDocuments.get(1), correspondenceDocuments.get(0)));
        }

        @Test
        void shouldSortCorrespondenceDocumentsLAWhenCorrespondenceDocumentsAreEmpty() {
            List<Element<SupportingEvidenceBundle>> correspondenceDocumentsLA = buildCorrespondenceDocuments("LA");
            CaseDetails caseDetails = caseDetails(List.of(), correspondenceDocumentsLA, familyManNumber, migrationId);

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

            assertThat(extractCaseData(response).getCorrespondenceDocumentsLA())
                .isEqualTo(List.of(correspondenceDocumentsLA.get(2), correspondenceDocumentsLA.get(1),
                    correspondenceDocumentsLA.get(0)));
            assertThat(extractCaseData(response).getCorrespondenceDocuments()).isEmpty();
        }

        @Test
        void shouldNotThrowExceptionWhenCorrespondenceDocumentsAreEmpty() {
            CaseDetails caseDetails = caseDetails(List.of(), List.of(), familyManNumber, migrationId);

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

            assertThat(extractCaseData(response).getCorrespondenceDocuments()).isEmpty();
            assertThat(extractCaseData(response).getCorrespondenceDocumentsLA()).isEmpty();
        }

        @Test
        void shouldNotMigrateCaseForIncorrectMigrationId() {
            List<Element<SupportingEvidenceBundle>> correspondenceDocuments = buildCorrespondenceDocuments("HMCTS");
            List<Element<SupportingEvidenceBundle>> correspondenceDocumentsLA = buildCorrespondenceDocuments("LA");

            String incorrectMigrationId = "incorrect migration id";
            CaseDetails caseDetails = caseDetails(
                correspondenceDocuments, correspondenceDocumentsLA, familyManNumber, incorrectMigrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getCorrespondenceDocuments()).isEqualTo(correspondenceDocuments);
            assertThat(extractedCaseData.getCorrespondenceDocumentsLA()).isEqualTo(correspondenceDocumentsLA);
        }

        private List<Element<SupportingEvidenceBundle>> buildCorrespondenceDocuments(String uploadedBy) {
            return wrapElements(SupportingEvidenceBundle.builder()
                    .name("document1").document(document)
                    .uploadedBy(uploadedBy)
                    .dateTimeUploaded(now().minusDays(3)).build(),
                SupportingEvidenceBundle.builder()
                    .name("document2").document(document)
                    .uploadedBy(uploadedBy)
                    .dateTimeUploaded(now().minusDays(2)).build(),
                SupportingEvidenceBundle.builder()
                    .name("document3").document(document)
                    .uploadedBy(uploadedBy)
                    .dateTimeUploaded(now().minusDays(1)).build());
        }

        private CaseDetails caseDetails(List<Element<SupportingEvidenceBundle>> correspondenceDocuments,
                                        List<Element<SupportingEvidenceBundle>> correspondenceDocumentsLA,
                                        String familyManCaseNumber,
                                        String migrationId) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .state(State.CASE_MANAGEMENT)
                .correspondenceDocuments(correspondenceDocuments)
                .correspondenceDocumentsLA(correspondenceDocumentsLA)
                .id(caseReference)
                .familyManCaseNumber(familyManCaseNumber)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

}
