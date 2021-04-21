package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @Nested
    class Fpla2982 {
        String migrationId = "FPLA-2982";

        @ParameterizedTest
        @ValueSource(longs = {
            1598429153622508L,
            1615191831533551L,
            1594384486007055L,
            1601977974423857L,
            1615571327261140L,
            1615476016828466L,
            1616507805759840L,
            1610015759403189L,
            1615994076934396L,
            1611613172339094L,
            1612440806991994L,
            1607004182103389L,
            1617045146450299L,
            1612433400114865L,
            1615890702114702L,
            1610018233059619L})
        void shouldMigrateMissingC2IdCase(Long caseId) {
            CaseDetails caseDetails = caseDetails(migrationId,
                wrapElements(createAdditionalApplicationBundle(createC2DocumentBundle(null),
                    createOtherApplicationBundle(null))), caseId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(!extractedCaseData.getAdditionalApplicationsBundle().isEmpty());

            extractedCaseData.getAdditionalApplicationsBundle()
                .forEach(bundle -> assertThat(bundle.getValue().getC2DocumentBundle().getId() != null));
        }

        @Test
        void shouldThrowExceptionForInvalidCaseId() {
            CaseDetails caseDetails = caseDetails(migrationId,
                wrapElements(createAdditionalApplicationBundle(createC2DocumentBundle(UUID.randomUUID()),
                    null)), 1234L);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Invalid case Id");
        }

        @Test
        void shouldThrowExceptionIfNoNullIdsFound() {
            CaseDetails caseDetails = caseDetails(migrationId,
                wrapElements(
                    createAdditionalApplicationBundle(createC2DocumentBundle(UUID.randomUUID()),
                        createOtherApplicationBundle(UUID.randomUUID())),
                    createAdditionalApplicationBundle(createC2DocumentBundle(UUID.randomUUID()),
                        null),
                    createAdditionalApplicationBundle(null,
                        createOtherApplicationBundle(UUID.randomUUID()))
                ), 1601977974423857L);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No c2DocumentBundle or otherApplicationsBundle found with missing Id");
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-9876";

            CaseDetails caseDetails = caseDetails(incorrectMigrationId,
                wrapElements(createAdditionalApplicationBundle(createC2DocumentBundle(null),
                    createOtherApplicationBundle(null))), 1615890702114702L);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(!extractedCaseData.getAdditionalApplicationsBundle().isEmpty());

            extractedCaseData.getAdditionalApplicationsBundle()
                .forEach(bundle -> assertThat(bundle.getValue().getC2DocumentBundle().getId() == null));
        }

        private C2DocumentBundle createC2DocumentBundle(UUID id) {
            return C2DocumentBundle.builder().id(id).build();
        }

        private OtherApplicationsBundle createOtherApplicationBundle(UUID id) {
            return OtherApplicationsBundle.builder().id(id).build();
        }

        private AdditionalApplicationsBundle createAdditionalApplicationBundle(
            C2DocumentBundle c2DocumentBundle,
            OtherApplicationsBundle otherApplicationsBundle) {
            return AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(c2DocumentBundle)
                .otherApplicationsBundle(otherApplicationsBundle)
                .build();
        }

        private CaseDetails caseDetails(String migrationId,
                                        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle,
                                        Long caseId) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .additionalApplicationsBundle(additionalApplicationsBundle)
                .id(caseId)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }
}
