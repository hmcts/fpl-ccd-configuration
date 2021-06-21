package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3088 {
        String familyManNumber = "CF21C50022";
        String migrationId = "FPLA-3088";
        UUID bundleId = UUID.fromString("1ccca4f7-40d5-4392-a199-ae9372f53d00");
        UUID c2ApplicationId = UUID.fromString("e3d5bac0-4ba6-48b6-b6d5-e60d5234a183");
        UUID anotherBundleId = UUID.randomUUID();
        UUID anotherC2ApplicationId = UUID.randomUUID();

        @Test
        void shouldRemoveAdditionalApplicationBundle() {
            List<Element<AdditionalApplicationsBundle>> bundles = buildAdditionalApplicationsBundle();
            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, migrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getAdditionalApplicationsBundle())
                .isEqualTo(List.of(bundles.get(1)));
        }

        @Test
        void shouldThrowExceptionWhenAdditionalApplicationBundleIdIsNotFound() {
            List<Element<AdditionalApplicationsBundle>> bundles = List.of(element(anotherBundleId,
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(anotherC2ApplicationId).build()).build()));

            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3088: Expected additional application bundle id to be %s but not found",
                    bundleId));
        }

        @Test
        void shouldThrowExceptionWhenC2ApplicationIdIsNotFound() {
            List<Element<AdditionalApplicationsBundle>> bundles = List.of(element(bundleId,
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(anotherC2ApplicationId).build()).build()),
                element(anotherBundleId, AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(UUID.randomUUID()).build()).build()));

            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3088: Expected c2 bundle Id to be %s but not found", c2ApplicationId));
        }

        @Test
        void shouldThrowExceptionWhenOtherApplicationExistInBundle() {
            List<Element<AdditionalApplicationsBundle>> bundles = List.of(element(bundleId,
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(c2ApplicationId).build())
                    .otherApplicationsBundle(OtherApplicationsBundle.builder().id(UUID.randomUUID()).build())
                    .build()),
                element(anotherBundleId, AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(anotherBundleId).build()).build()));

            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Migration FPLA-3088: Unexpected other application bundle");
        }

        @Test
        void shouldThrowExceptionForIncorrectFamilyManNumber() {
            List<Element<AdditionalApplicationsBundle>> bundles = List.of(element(bundleId,
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(anotherC2ApplicationId).build()).build()),
                element(anotherBundleId, AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(UUID.randomUUID()).build()).build()));

            String incorrectFamilyManNum = "12456";
            CaseDetails caseDetails = caseDetails(bundles, incorrectFamilyManNum, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3088: Expected family man case number to be %s but was %s",
                    familyManNumber, incorrectFamilyManNum));
        }

        @Test
        void shouldNotRemoveAdditionalApplicationBundleForIncorrectMigrationId() {
            List<Element<AdditionalApplicationsBundle>> bundles = buildAdditionalApplicationsBundle();
            String incorrectMigrationId = "incorrect migration id";
            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, incorrectMigrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getAdditionalApplicationsBundle()).isEqualTo(bundles);
        }

        private CaseDetails caseDetails(List<Element<AdditionalApplicationsBundle>> bundles,
                                        String familyManNumber,
                                        String migrationId) {

            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .additionalApplicationsBundle(bundles)
                .familyManCaseNumber(familyManNumber)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private List<Element<AdditionalApplicationsBundle>> buildAdditionalApplicationsBundle() {
            return List.of(element(bundleId, AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(c2ApplicationId).build()).build()),
                element(anotherBundleId, AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(anotherC2ApplicationId).build()).build()));
        }

    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3089 {
        String familyManNumber = "WR21C50042";
        String migrationId = "FPLA-3089";
        UUID bundleId = UUID.fromString("6dc62c7b-47a5-4e26-8ce3-99a697f72454");
        UUID c2ApplicationId = UUID.fromString("2b2159aa-6ab8-4b63-b65b-08cb896de2ec");
        UUID incorrectBundleId1 = UUID.randomUUID();
        UUID incorrectBundleId2 = UUID.randomUUID();
        UUID incorrectC2ApplicationId1 = UUID.randomUUID();
        UUID incorrectC2ApplicationId2 = UUID.randomUUID();

        @Test
        void shouldRemoveAdditionalApplicationBundle() {
            List<Element<AdditionalApplicationsBundle>> bundles = buildAdditionalApplicationsBundle();
            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, migrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getAdditionalApplicationsBundle())
                .isEqualTo(List.of(bundles.get(0), bundles.get(1)));
        }

        @Test
        void shouldNotRemoveAdditionalApplicationBundleForIncorrectMigrationId() {
            List<Element<AdditionalApplicationsBundle>> bundles = buildAdditionalApplicationsBundle();
            String incorrectMigrationId = "incorrect migration id";
            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, incorrectMigrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getAdditionalApplicationsBundle()).isEqualTo(bundles);
        }

        @Test
        void shouldThrowExceptionWhenAdditionalApplicationBundleIdIsNotFound() {
            List<Element<AdditionalApplicationsBundle>> bundles = List.of(element(incorrectBundleId2,
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(incorrectC2ApplicationId1).build()).build()));

            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3089: Expected additional application bundle id to be %s but not found",
                    bundleId));
        }

        @Test
        void shouldThrowExceptionWhenC2ApplicationIdIsNotFound() {
            List<Element<AdditionalApplicationsBundle>> bundles = List.of(element(bundleId,
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(incorrectC2ApplicationId1).build()).build()),
                element(incorrectBundleId2, AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(UUID.randomUUID()).build()).build()));

            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3089: Expected c2 bundle Id to be %s but not found", c2ApplicationId));
        }

        @Test
        void shouldThrowExceptionWhenOtherApplicationExistInBundle() {
            List<Element<AdditionalApplicationsBundle>> bundles = List.of(element(bundleId,
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(c2ApplicationId).build())
                    .otherApplicationsBundle(OtherApplicationsBundle.builder().id(UUID.randomUUID()).build())
                    .build()),
                element(incorrectBundleId2, AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(incorrectBundleId2).build()).build()));

            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Migration FPLA-3089: Unexpected other application bundle");
        }

        @Test
        void shouldThrowExceptionForIncorrectFamilyManNumber() {
            List<Element<AdditionalApplicationsBundle>> bundles = buildAdditionalApplicationsBundle();

            String incorrectFamilyManNum = "12456";
            CaseDetails caseDetails = caseDetails(bundles, incorrectFamilyManNum, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3089: Expected family man case number to be %s but was %s",
                    familyManNumber, incorrectFamilyManNum));
        }

        private CaseDetails caseDetails(List<Element<AdditionalApplicationsBundle>> bundles,
                                        String familyManNumber,
                                        String migrationId) {

            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .additionalApplicationsBundle(bundles)
                .familyManCaseNumber(familyManNumber)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private List<Element<AdditionalApplicationsBundle>> buildAdditionalApplicationsBundle() {
            return List.of(element(incorrectBundleId1, AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(incorrectC2ApplicationId1).build()).build()),
                element(incorrectBundleId2, AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(incorrectC2ApplicationId2).build()).build()),
                element(bundleId, AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder().id(c2ApplicationId).build()).build()));
        }

    }

}
