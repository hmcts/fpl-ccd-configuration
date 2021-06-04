package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.C2ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.Collections;
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
    class Fpla3093 {
        String familyManNumber = "SA21C50024";
        String migrationId = "FPLA-3093";

        private DocumentReference supportingDocument = testDocumentReference("Correct c2 document");
        private DocumentReference c2Document = testDocumentReference("Incorrect c2 document");

        @Test
        void shouldRemoveC2DocumentAtIndex0() {
            List<Element<AdditionalApplicationsBundle>> additionalApplications = wrapElements(
                buildAdditionalApplicationsBundle(WITH_NOTICE));

            CaseDetails caseDetails = caseDetails(additionalApplications, migrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getAdditionalApplicationsBundle()).isEmpty();
        }

        @Test
        void shouldRemoveC2DocumentAtIndex0AndLeaveOtherElementsUnModified() {
            AdditionalApplicationsBundle bundle1 = buildAdditionalApplicationsBundle(WITH_NOTICE);
            AdditionalApplicationsBundle bundle2 = buildAdditionalApplicationsBundle(WITHOUT_NOTICE);

            List<Element<AdditionalApplicationsBundle>> additionalApplications = wrapElements(
                bundle1, bundle2);

            CaseDetails caseDetails = caseDetails(additionalApplications, migrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            List<AdditionalApplicationsBundle> extractedApplicationBundle = unwrapElements(extractedCaseData
                .getAdditionalApplicationsBundle());

            assertThat(extractedApplicationBundle.size()).isEqualTo(1);
            assertThat(extractedApplicationBundle.get(0)).isEqualTo(bundle2);
        }

        @Test
        void shouldNotMigrateCaseIfMigrationIdIsIncorrect() {
            String incorrectMigrationId = "FPLA-1111";
            List<Element<AdditionalApplicationsBundle>> additionalApplications = wrapElements(
                buildAdditionalApplicationsBundle(WITH_NOTICE));

            CaseDetails caseDetails = caseDetails(additionalApplications, incorrectMigrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getAdditionalApplicationsBundle()).isEqualTo(additionalApplications);
        }

        @Test
        void shouldThrowAnExceptionIfCaseContainsNoAdditionalApplications() {
            List<Element<AdditionalApplicationsBundle>> additionalApplications = Collections.emptyList();

            CaseDetails caseDetails = caseDetails(additionalApplications, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format("Migration failed on case %s: Case has %s additional applications",
                    familyManNumber, additionalApplications.size()));
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

        private AdditionalApplicationsBundle buildAdditionalApplicationsBundle(C2ApplicationType type) {
            return AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .type(type)
                    .document(c2Document)
                    .usePbaPayment(YES.getValue())
                    .supportingEvidenceBundle(wrapElements(SupportingEvidenceBundle.builder()
                        .document(supportingDocument)
                        .build()))
                    .build())
                .build();
        }

    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla2991 {
        String familyManNumber = "SA21C50011";
        String migrationId = "FPLA-2991";

        private DocumentReference supportingDocument = testDocumentReference("Correct c2 document");
        private DocumentReference c2Document = testDocumentReference("Incorrect c2 document");

        @Test
        void shouldRemoveSupportingEvidenceDocumentWithCorrectID() {
            UUID secondBundleID = UUID.fromString("1bae342e-f73c-4ef3-b7e2-044d6c618825");
            UUID supportingEvidenceID = UUID.fromString("1bae342e-f73c-4ef3-b7e2-044d6c618825");
            List<Element<AdditionalApplicationsBundle>> additionalApplications = List.of(element(UUID.randomUUID(),
                buildAdditionalApplicationsBundle(supportingEvidenceID)), element(secondBundleID,
                buildAdditionalApplicationsBundle(supportingEvidenceID)));

            CaseDetails caseDetails = caseDetails(additionalApplications, migrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            AdditionalApplicationsBundle expectedBundle = AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(c2Document)
                    .type(WITH_NOTICE)
                    .supportingEvidenceBundle(null)
                    .usePbaPayment(YES.getValue())
                    .build()).build();

            assertThat(extractedCaseData.getAdditionalApplicationsBundle().get(0))
                .isEqualTo(additionalApplications.get(0));
            assertThat(extractedCaseData.getAdditionalApplicationsBundle().get(1))
                .isEqualTo(additionalApplications.get(1));
        }

        @Test
        void shouldNotMigrateCaseIfMigrationIdIsIncorrect() {
            String incorrectMigrationId = "FPLA-1111";
            List<Element<AdditionalApplicationsBundle>> additionalApplications = wrapElements(
                buildAdditionalApplicationsBundle(UUID.randomUUID()));

            CaseDetails caseDetails = caseDetails(additionalApplications, incorrectMigrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getAdditionalApplicationsBundle()).isEqualTo(additionalApplications);
        }

        @Test
        void shouldThrowAnExceptionIfIncorrectIDForSecondBundle() {
            UUID wrongID = UUID.randomUUID();
            UUID secondBundleID = UUID.fromString("1bae342e-f73c-4ef3-b7e2-044d6c618825");
            UUID supportingEvidenceID = UUID.fromString("1bae342e-f73c-4ef3-b7e2-044d6c618825");
            List<Element<AdditionalApplicationsBundle>> additionalApplications = List.of(element(UUID.randomUUID(),
                buildAdditionalApplicationsBundle(UUID.randomUUID())), element(wrongID,
                buildAdditionalApplicationsBundle(supportingEvidenceID)));

            CaseDetails caseDetails = caseDetails(additionalApplications, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format("Migration failed on case SA21C50011: Expected " + secondBundleID
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

        private AdditionalApplicationsBundle buildAdditionalApplicationsBundle(UUID id) {
            return AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .document(c2Document)
                    .type(WITH_NOTICE)
                    .supportingEvidenceBundle(List.of(element(id, SupportingEvidenceBundle.builder()
                        .document(supportingDocument)
                        .build()), element(UUID.randomUUID(), SupportingEvidenceBundle.builder()
                        .document(supportingDocument).build())))
                    .usePbaPayment(YES.getValue())
                    .build()).build();
        }
    }
}
