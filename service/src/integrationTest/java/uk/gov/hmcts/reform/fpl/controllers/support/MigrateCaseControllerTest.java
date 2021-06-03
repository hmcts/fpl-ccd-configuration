package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
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
    class Fpla3037 {
        UUID c2DocumentBundleUuid = UUID.randomUUID();
        UUID supportingEvidenceBundleUuid = UUID.randomUUID();
        UUID supportingEvidenceBundleToRemoveUuid = UUID.fromString("4885a0e2-fd88-4614-9c35-6c61d6b5e422");
        LocalDateTime localDateTime = LocalDateTime.now();
        String migrationId = "FPLA-3037";

        @Test
        void shouldRemoveExpectedSupportingEvidenceBundles() {
            AdditionalApplicationsBundle additionalApplicationsBundle = buildAdditionalApplicationsBundle();

            AdditionalApplicationsBundle expectedAdditionalApplicationsBundle =
                buildExpectedAdditionalApplicationsBundle();

            CaseDetails caseDetails = caseDetails(additionalApplicationsBundle, migrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));
            AdditionalApplicationsBundle extractedAdditionalApplicationsBundle =
                extractedCaseData.getAdditionalApplicationsBundle().get(0).getValue();

            assertThat(extractedAdditionalApplicationsBundle).isEqualTo(expectedAdditionalApplicationsBundle);
        }

        @Test
        void shouldNotRemoveSupportingEvidenceBundles() {
            AdditionalApplicationsBundle additionalApplicationsBundle = buildExpectedAdditionalApplicationsBundle();

            CaseDetails caseDetails = caseDetails(additionalApplicationsBundle, migrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));
            AdditionalApplicationsBundle extractedAdditionalApplicationsBundle =
                extractedCaseData.getAdditionalApplicationsBundle().get(0).getValue();

            assertThat(extractedAdditionalApplicationsBundle).isEqualTo(additionalApplicationsBundle);
        }

        @Test
        void shouldNotMigrateCaseIfMigrationIdIsIncorrect() {
            String incorrectMigrationId = "FPLA-1111";

            AdditionalApplicationsBundle additionalApplicationsBundle = buildAdditionalApplicationsBundle();

            CaseDetails caseDetails = caseDetails(additionalApplicationsBundle, incorrectMigrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getAdditionalApplicationsBundle()).isEqualTo(wrapElements(
                additionalApplicationsBundle));
        }

        private CaseDetails caseDetails(AdditionalApplicationsBundle additionalApplicationsBundle,
                                        String migrationId) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .additionalApplicationsBundle(wrapElements(additionalApplicationsBundle))
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private AdditionalApplicationsBundle buildExpectedAdditionalApplicationsBundle() {
            return AdditionalApplicationsBundle
                .builder()
                .c2DocumentBundle(
                    C2DocumentBundle.builder()
                        .id(c2DocumentBundleUuid)
                        .author("author")
                        .supportingEvidenceBundle(List.of(
                            element(supportingEvidenceBundleUuid, SupportingEvidenceBundle.builder()
                                .uploadedBy("test@test.co.uk")
                                .dateTimeUploaded(localDateTime)
                                .document(DocumentReference.builder().build())
                                .build())
                        )).build()
                ).build();
        }

        private AdditionalApplicationsBundle buildAdditionalApplicationsBundle() {
            return AdditionalApplicationsBundle
                .builder()
                .c2DocumentBundle(
                    C2DocumentBundle.builder()
                        .id(c2DocumentBundleUuid)
                        .author("author")
                        .supportingEvidenceBundle(List.of(
                            element(supportingEvidenceBundleToRemoveUuid, SupportingEvidenceBundle.builder()
                                .uploadedBy("test@test.co.uk")
                                .dateTimeUploaded(localDateTime)
                                .build()),
                            element(supportingEvidenceBundleUuid, SupportingEvidenceBundle.builder()
                                .uploadedBy("test@test.co.uk")
                                .dateTimeUploaded(localDateTime)
                                .document(DocumentReference.builder().build())
                                .build())
                        )).build()
                ).build();
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3080 {
        String familyManNumber = "SA21C50024";
        String migrationId = "FPLA-3080";
        private UUID firstBundleID = UUID.fromString("fbf05208-f5dd-4942-b735-9aa226d73a2e");
        private UUID secondBundleID = UUID.fromString("4e4def36-2323-4e95-b93a-2f46fc4d6fc0");
        private UUID supportingEvidenceID = UUID.fromString("3f3a183e-44ab-4e63-ac27-0ca40f3058ff");

        private DocumentReference supportingDocument = testDocumentReference("Correct c2 document");
        private DocumentReference c2Document = testDocumentReference("Incorrect c2 document");

        @Test
        void shouldReplaceC2DocumentWithSupportingDocumentWithCorrectID() {
            List<Element<AdditionalApplicationsBundle>> additionalApplications = List.of(element(firstBundleID,
                expectedFirstAdditionalApplicationsBundle()), element(secondBundleID,
                buildSecondAdditionalApplicationsBundle()));

            CaseDetails caseDetails = caseDetails(additionalApplications, migrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            C2DocumentBundle expectedBundle = C2DocumentBundle.builder()
                .document(supportingDocument)
                .type(WITH_NOTICE)
                .supportingEvidenceBundle(null)
                .usePbaPayment(YES.getValue())
                .build();

            List<AdditionalApplicationsBundle> extractedApplicationBundle = unwrapElements(extractedCaseData
                .getAdditionalApplicationsBundle());

            //assertThat(extractedApplicationBundle.get(0)).isEqualTo(additionalApplications.get(0).getValue());
            assertThat(extractedApplicationBundle.get(1).getC2DocumentBundle()).isEqualTo(expectedBundle);
        }

        @Test
        void shouldRevertC2DocumentAndSupportingDocumentsToOldValuesWithCorrectID() {
            List<Element<AdditionalApplicationsBundle>> additionalApplications = List.of(element(firstBundleID,
                buildFirstAdditionalApplicationsBundle()), element(secondBundleID,
                expectedSecondAdditionalApplicationsBundle()));

            CaseDetails caseDetails = caseDetails(additionalApplications, migrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            List<AdditionalApplicationsBundle> extractedApplicationBundle = unwrapElements(extractedCaseData
                .getAdditionalApplicationsBundle());

            assertThat(extractedApplicationBundle.get(1)).isEqualTo(additionalApplications.get(1).getValue());
            assertThat(extractedApplicationBundle.get(0)).isEqualTo(expectedFirstAdditionalApplicationsBundle());
        }

        @Test
        void shouldNotMigrateCaseIfMigrationIdIsIncorrect() {
            String incorrectMigrationId = "FPLA-1111";
            List<Element<AdditionalApplicationsBundle>> additionalApplications = List.of(element(firstBundleID,
                buildFirstAdditionalApplicationsBundle()), element(secondBundleID,
                expectedSecondAdditionalApplicationsBundle()));

            CaseDetails caseDetails = caseDetails(additionalApplications, incorrectMigrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getAdditionalApplicationsBundle()).isEqualTo(additionalApplications);
        }

        @Test
        void shouldThrowAnExceptionIfIncorrectIDForFirstBundle() {
            UUID wrongID = UUID.randomUUID();
            List<Element<AdditionalApplicationsBundle>> additionalApplications = List.of(element(wrongID,
                buildFirstAdditionalApplicationsBundle()), element(secondBundleID,
                expectedSecondAdditionalApplicationsBundle()));

            CaseDetails caseDetails = caseDetails(additionalApplications, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format("Migration failed on case SA21C50024: Expected " + firstBundleID
                        + " but got " + wrongID, familyManNumber, additionalApplications.get(0).getId()));
        }

        @Test
        void shouldThrowAnExceptionIfIncorrectIDForSecondBundle() {
            UUID wrongID = UUID.randomUUID();
            List<Element<AdditionalApplicationsBundle>> additionalApplications = List.of(element(firstBundleID,
                buildFirstAdditionalApplicationsBundle()), element(wrongID,
                expectedSecondAdditionalApplicationsBundle()));

            CaseDetails caseDetails = caseDetails(additionalApplications, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format("Migration failed on case SA21C50024: Expected " + secondBundleID
                        + " but got " + wrongID,
                    familyManNumber, additionalApplications.get(1).getId()));
        }

        private CaseDetails caseDetails(List<Element<AdditionalApplicationsBundle>> additionalApplications,
                                        String migrationId) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .state(State.CASE_MANAGEMENT)
                .additionalApplicationsBundle(additionalApplications)
                .familyManCaseNumber(familyManNumber)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private AdditionalApplicationsBundle buildFirstAdditionalApplicationsBundle() {
            return AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .type(WITHOUT_NOTICE)
                    .document(testDocumentReference())
                    .usePbaPayment(YES.getValue())
                    .supportingEvidenceBundle(null)
                    .build())
                .build();
        }

        private AdditionalApplicationsBundle buildSecondAdditionalApplicationsBundle() {
            return AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .type(WITH_NOTICE)
                    .document(c2Document)
                    .usePbaPayment(YES.getValue())
                    .supportingEvidenceBundle(List.of(element(supportingEvidenceID, SupportingEvidenceBundle.builder()
                        .document(supportingDocument)
                        .build())))
                    .build())
                .build();
        }

        private AdditionalApplicationsBundle expectedFirstAdditionalApplicationsBundle() {
            return AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .type(WITHOUT_NOTICE)
                    .document(buildExpectedC2Document())
                    .usePbaPayment(YES.getValue())
                    .supportingEvidenceBundle(buildSupportingEvidenceBundle())
                    .build())
                .build();
        }

        private AdditionalApplicationsBundle expectedSecondAdditionalApplicationsBundle() {
            return AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(supportingDocument)
                    .type(WITH_NOTICE)
                    .supportingEvidenceBundle(null)
                    .usePbaPayment(YES.getValue())
                    .build()).build();
        }

        private DocumentReference buildExpectedC2Document() {
            return DocumentReference.builder()
                .filename("S45C-921052410100.pdf")
                .url("http://dm-store-prod.service.core-compute-prod.internal/documents/5ddafb9c-1396-44c1-a4dc-25204b0989f0")
                .binaryUrl("http://dm-store-prod.service.core-compute-prod.internal/documents/5ddafb9c-1396-44c1-a4dc-25204b0989f0/binary")
                .build();
        }

        private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
            SupportingEvidenceBundle firstBundle = SupportingEvidenceBundle.builder()
                .name("Position Statement for C2")
                .document(DocumentReference.builder()
                    .url("http://dm-store-prod.service.core-compute-prod.internal/documents/d448738d-51fe-439e-9a0b-5ecc0bb378b6")
                    .binaryUrl("http://dm-store-prod.service.core-compute-prod.internal/documents/d448738d-51fe-439e-9a0b-5ecc0bb378b6/binary")
                    .filename("Position Statement for C2.docx")
                    .build())
                .uploadedBy("HMCTS")
                .dateTimeUploaded(LocalDateTime.of(2021, 05, 24, 10, 23, 32))
                .build();

            SupportingEvidenceBundle secondBundle = SupportingEvidenceBundle.builder()
                .name("Draft LOI")
                .document(DocumentReference.builder()
                    .url("http://dm-store-prod.service.core-compute-prod.internal/documents/d8357dbc-e3bd-464f-8edb-aac0fe1c58c2")
                    .binaryUrl("http://dm-store-prod.service.core-compute-prod.internal/documents/d8357dbc-e3bd-464f-8edb-aac0fe1c58c2/binary")
                    .filename("AMO0030002 Draft LOI.docx")
                    .build())
                .uploadedBy("HMCTS")
                .dateTimeUploaded(LocalDateTime.of(2021, 05, 24, 10, 23, 32))
                .build();

            SupportingEvidenceBundle thirdBundle = SupportingEvidenceBundle.builder()
                .name("CV")
                .document(DocumentReference.builder()
                    .url("http://dm-store-prod.service.core-compute-prod.internal/documents/6b6b5071-e097-4074-b317-b00dc3cfa89c")
                    .binaryUrl("http://dm-store-prod.service.core-compute-prod.internal/documents/6b6b5071-e097-4074-b317-b00dc3cfa89c/binary")
                    .filename("AMO0030002 Medico-legal_CV-May2021.doc")
                    .build())
                .uploadedBy("HMCTS")
                .dateTimeUploaded(LocalDateTime.of(2021, 05, 24, 10, 23, 32))
                .build();

            return wrapElements(firstBundle, secondBundle, thirdBundle);
        }
    }
}
