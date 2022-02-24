package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final String INVALID_MIGRATION_ID = "invalid id";

    @Test
    void shouldThrowExceptionWhenMigrationNotMappedForMigrationID() {
        CaseData caseData = CaseData.builder().build();

        assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, INVALID_MIGRATION_ID)))
            .getRootCause()
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No migration mapped to " + INVALID_MIGRATION_ID);
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl82 {
        private final String migrationId = "DFPL-82";

        private CourtBundle createCourtBundle(String hearing, String fileName, String fileUrl, String binaryUrl) {
            return CourtBundle.builder()
                .hearing(hearing)
                .document(DocumentReference.builder()
                    .filename(fileName)
                    .url(fileUrl)
                    .binaryUrl(binaryUrl)
                    .build())
                .confidential(List.of())
                .build();
        }

        @Test
        void shouldPerformMigration() {
            CourtBundle courtBundle1 = createCourtBundle("hearing 1",
                "doc1", "url", "binaryUrl");
            CourtBundle courtBundle2 = createCourtBundle("hearing 1",
                "doc3", "url3", "binaryUrl3");
            CourtBundle courtBundle3 = createCourtBundle("hearing 2",
                "doc2", "url2", "binaryUrl2");

            List<CourtBundle> courtBundles = List.of(courtBundle1, courtBundle2, courtBundle3);

            CaseData caseData = CaseData.builder()
                .id(1L)
                .state(State.SUBMITTED)
                .courtBundleList(wrapElements(courtBundles)).build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );


            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getCourtBundleList()).isNull();
            assertThat(responseData.getCourtBundleListV2())
                .extracting(Element::getValue)
                .contains(
                    HearingCourtBundle.builder()
                        .hearing("hearing 1")
                        .courtBundle(wrapElements(List.of(courtBundle1, courtBundle2)))
                        .courtBundleNC(wrapElements(List.of(courtBundle1, courtBundle2)))
                        .build(),
                    HearingCourtBundle.builder()
                        .hearing("hearing 2")
                        .courtBundle(wrapElements(List.of(courtBundle3)))
                        .courtBundleNC(wrapElements(List.of(courtBundle3)))
                        .build()
                );

            // now roll back the migration
            String rollBackMigrationId = "DFPL-82-rollback";
            AboutToStartOrSubmitCallbackResponse rollBackResponse = postAboutToSubmitEvent(
                buildCaseDetails(responseData, rollBackMigrationId)
            );

            CaseData rollbackResponseData = extractCaseData(rollBackResponse);
            assertThat(rollbackResponseData.getCourtBundleListV2()).isEmpty(); // default value of courtBundleListV2 is empty
            assertThat(unwrapElements(rollbackResponseData.getCourtBundleList())).containsExactlyInAnyOrder(courtBundle1, courtBundle2, courtBundle3);
        }

        @Test
        void shouldSkipMigration() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .state(State.SUBMITTED)
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );


            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getCourtBundleList()).isNull();
            assertThat(responseData.getCourtBundleListV2()).isEmpty();
        }
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }
}
