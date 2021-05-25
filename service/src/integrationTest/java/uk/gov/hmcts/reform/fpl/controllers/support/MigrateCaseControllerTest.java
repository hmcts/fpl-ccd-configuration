package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.casesubmission.CaseSubmissionService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @MockBean
    private CaseSubmissionService caseSubmissionService;

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
    class Fpla3087 {
        private final long caseId = 1618497329043582L;
        private final String migrationId = "FPLA-3087";
        private final Document document = testDocument();
        private final DocumentReference c110a = buildFromDocument(document);
        private final DocumentReference oldC110a = testDocumentReference();


        @BeforeEach
        void setUp() {
            when(caseSubmissionService.generateSubmittedFormPDF(any(CaseData.class), eq(false)))
                .thenReturn(document);
        }

        @Test
        void shouldRegenerateC110a() {
            Map<String, Object> data = Map.of(
                "migrationId", migrationId,
                "submittedForm", oldC110a
            );

            CaseDetails caseDetails = CaseDetails.builder().id(caseId).data(data).build();

            CaseData caseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(caseData.getSubmittedForm()).isEqualTo(c110a);
        }

        @Test
        void shouldNotRegenerateC110aWhenCaseIdDoesNotMatch() {
            Map<String, Object> data = Map.of(
                "migrationId", migrationId,
                "submittedForm", oldC110a
            );

            CaseDetails caseDetails = CaseDetails.builder().id(2L).data(data).build();

            CaseData caseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(caseData.getSubmittedForm()).isEqualTo(oldC110a);
        }

        @Test
        void shouldNotRegenerateC110aWhenMigrationIdDoesNotMatch() {
            Map<String, Object> data = Map.of(
                "migrationId", "YYYY",
                "submittedForm", oldC110a
            );

            CaseDetails caseDetails = CaseDetails.builder().id(caseId).data(data).build();

            CaseData caseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(caseData.getSubmittedForm()).isEqualTo(oldC110a);
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3080 {
        String familyManNumber = "SA21C50024";
        String migrationId = "FPLA-3080";

        private DocumentReference supportingDocument = testDocumentReference("Correct c2 document");
        private DocumentReference c2Document = testDocumentReference("Incorrect c2 document");

        @Test
        void shouldReplaceC2DocumentWithSupportingDocument() {
            List<Element<AdditionalApplicationsBundle>> additionalApplications = wrapElements(
                buildAdditionalApplicationsBundle());

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

            assertThat(extractedApplicationBundle.get(0).getC2DocumentBundle()).isEqualTo(expectedBundle);
        }

        @Test
        void shouldReplaceC2DocumentWithSupportingDocumentAtIndex0AndLeaveOtherElementsUnModified() {
            List<Element<AdditionalApplicationsBundle>> additionalApplications = wrapElements(
                buildAdditionalApplicationsBundle(), buildAdditionalApplicationsBundle());

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

            assertThat(extractedApplicationBundle.size()).isEqualTo(2);
            assertThat(extractedApplicationBundle.get(0).getC2DocumentBundle()).isEqualTo(expectedBundle);
            assertThat(extractedApplicationBundle.get(1)).isEqualTo(additionalApplications.get(1).getValue());
        }

        @Test
        void shouldNotMigrateCaseIfMigrationIdIsIncorrect() {
            String incorrectMigrationId = "FPLA-1111";
            List<Element<AdditionalApplicationsBundle>> additionalApplications = wrapElements(
                buildAdditionalApplicationsBundle());

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

        private AdditionalApplicationsBundle buildAdditionalApplicationsBundle() {
            return AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .type(WITH_NOTICE)
                    .document(c2Document)
                    .usePbaPayment(YES.getValue())
                    .supportingEvidenceBundle(wrapElements(SupportingEvidenceBundle.builder()
                        .document(supportingDocument)
                        .build()))
                    .build())
                .build();
        }

    }
}
