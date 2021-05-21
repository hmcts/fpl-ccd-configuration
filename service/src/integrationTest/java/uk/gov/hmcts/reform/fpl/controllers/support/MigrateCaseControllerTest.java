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
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

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

            AdditionalApplicationsBundle expectedAdditionalApplicationsBundle =
                buildExpectedAdditionalApplicationsBundle();

            CaseDetails caseDetails = caseDetails(additionalApplicationsBundle, migrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));
            AdditionalApplicationsBundle extractedAdditionalApplicationsBundle =
                extractedCaseData.getAdditionalApplicationsBundle().get(0).getValue();

            assertThat(extractedAdditionalApplicationsBundle).isEqualTo(expectedAdditionalApplicationsBundle);
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
}
