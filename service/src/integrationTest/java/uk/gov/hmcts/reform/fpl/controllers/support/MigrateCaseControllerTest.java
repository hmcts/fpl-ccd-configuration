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
import uk.gov.hmcts.reform.fpl.model.common.Element;

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

        @Test
        void shouldMigrateMissingC2IdCase() {
            CaseDetails caseDetails = caseDetails(migrationId,
                wrapElements(createAdditionalApplicationBundle(createC2DocumentBundle(null)))
                , 1234567L);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(!extractedCaseData.getAdditionalApplicationsBundle().isEmpty());

            extractedCaseData.getAdditionalApplicationsBundle()
                .forEach(bundle -> assertThat(bundle.getValue().getC2DocumentBundle().getId() != null));
        }

        @Test
        void shouldThrowExceptionIfNoC2WithNullIdFound() {
            CaseDetails caseDetails = caseDetails(migrationId,
                wrapElements(createAdditionalApplicationBundle(createC2DocumentBundle(UUID.randomUUID())))
                , 7891L);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No c2DocumentBundle found with missing Id");
        }

        private C2DocumentBundle createC2DocumentBundle(UUID id) {
            return C2DocumentBundle.builder().id(id).build();
        }

        private AdditionalApplicationsBundle createAdditionalApplicationBundle(C2DocumentBundle c2DocumentBundle) {
            return AdditionalApplicationsBundle.builder().c2DocumentBundle(c2DocumentBundle).build();
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
