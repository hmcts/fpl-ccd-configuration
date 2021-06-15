package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @Nested
    class Fpla3125 {
        private final String migrationId = "FPLA-3125";
        private final UUID bundleId = fromString("b02898e7-46dc-47ce-9639-9e5b04d03b9e");
        private final UUID c2Id = fromString("4b725c8a-3496-4f28-83f1-95d4838a533a");
        private final String c2DocId = "b444c4fb-362b-4e27-b7d8-61996b3f6e0d";
        private final String familyManCaseNumber = "SA20C50050";
        private final String invalidId = "00000000-0000-0000-0000-000000000000";
        private final Element<AdditionalApplicationsBundle> randomBundle = element(
            UUID.randomUUID(), AdditionalApplicationsBundle.builder().build()
        );

        @Test
        void shouldMigrate() {
            CaseData caseData = extractCaseData(postAboutToSubmitEvent(caseDetails(standardCaseData(), migrationId)));

            assertThat(caseData.getAdditionalApplicationsBundle()).isEqualTo(List.of(randomBundle));
        }

        @Test
        void shouldNotMigrateWhenInvalidMigrationId() {
            CaseData.CaseDataBuilder caseData = standardCaseData();
            CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseDetails(caseData, "")));

            List<Element<AdditionalApplicationsBundle>> originalBundles = caseData.build()
                .getAdditionalApplicationsBundle();
            assertThat(responseData.getAdditionalApplicationsBundle()).isEqualTo(originalBundles);
        }

        @Test
        void shouldThrowExceptionWhenInvalidFamilyManNumber() {
            CaseData.CaseDataBuilder caseData = standardCaseData()
                .familyManCaseNumber("bad number");

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration FPLA-3125: Expected family man case number to be SA20C50050 but was bad number");
        }

        @Test
        void shouldThrowExceptionWhenInvalidBundleId() {
            CaseData.CaseDataBuilder caseData = standardCaseData()
                .additionalApplicationsBundle(buildAdditionalApplicationBundle(fromString(invalidId), c2Id, c2DocId));

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration FPLA-3125: Expected bundle id to be b02898e7-46dc-47ce-9639-9e5b04d03b9e"
                            + " but was 00000000-0000-0000-0000-000000000000");
        }

        @Test
        void shouldThrowExceptionWhenInvalidC2Id() {
            CaseData.CaseDataBuilder caseData = standardCaseData()
                .additionalApplicationsBundle(buildAdditionalApplicationBundle(
                    bundleId, fromString(invalidId), c2DocId
                ));

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration FPLA-3125: Expected c2 id to be 4b725c8a-3496-4f28-83f1-95d4838a533a"
                            + " but was 00000000-0000-0000-0000-000000000000");
        }

        @Test
        void shouldThrowExceptionWhenInvalidC2DocId() {
            CaseData.CaseDataBuilder caseData = standardCaseData()
                .additionalApplicationsBundle(buildAdditionalApplicationBundle(bundleId, c2Id, invalidId));

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration FPLA-3125: Expected doc id to be b444c4fb-362b-4e27-b7d8-61996b3f6e0d"
                            + " but was some-url/00000000-0000-0000-0000-000000000000");
        }

        @Test
        void shouldThrowExceptionWhenIncorrectBundleSize() {
            CaseData.CaseDataBuilder caseData = standardCaseData()
                .additionalApplicationsBundle(wrapElements(mock(AdditionalApplicationsBundle.class)));

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration FPLA-3125: Expected additional applications bundle size to be 2 but was 1");
        }

        private CaseData.CaseDataBuilder standardCaseData() {
            return CaseData.builder()
                .familyManCaseNumber(familyManCaseNumber)
                .additionalApplicationsBundle(buildAdditionalApplicationBundle(bundleId, c2Id, c2DocId));
        }

        private List<Element<AdditionalApplicationsBundle>> buildAdditionalApplicationBundle(UUID bundleId, UUID c2Id,
                                                                                             String c2DocId) {
            return List.of(
                randomBundle,
                element(bundleId, AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(C2DocumentBundle.builder()
                        .id(c2Id)
                        .document(DocumentReference.builder()
                            .url("some-url/" + c2DocId)
                            .build())
                        .build())
                    .build()
                )
            );
        }

        private CaseDetails caseDetails(CaseData.CaseDataBuilder caseData, String migrationId) {
            CaseDetails caseDetails = asCaseDetails(caseData.build());
            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }
}
