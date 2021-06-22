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
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

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
            List<Element<AdditionalApplicationsBundle>> bundles = buildAdditionalApplicationsBundles(anotherBundleId, c2ApplicationId);

            CaseDetails caseDetails = caseDetails(bundles, familyManNumber, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format(
                    "Migration FPLA-3093: Expected additional application bundle id to be %s but not found",
                    additionalApplicationBundleId));
        }

        @Test
        void shouldThrowExceptionWhenC2ApplicationIdIsNotFound() {
            List<Element<AdditionalApplicationsBundle>> bundles = buildAdditionalApplicationsBundles(additionalApplicationBundleId, anotherC2ApplicationId);

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
