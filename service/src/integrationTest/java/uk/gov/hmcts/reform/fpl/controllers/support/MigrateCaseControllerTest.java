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
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
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
        private static final String CONFIDENTIAL = "CONFIDENTIAL";

        private CourtBundle createCourtBundle(String hearing, String fileName, String fileUrl, String binaryUrl) {
            return CourtBundle.builder()
                .hearing(hearing)
                .document(DocumentReference.builder()
                    .filename(fileName)
                    .url(fileUrl)
                    .binaryUrl(binaryUrl)
                    .build())
                .confidential(List.of(CONFIDENTIAL))
                .build();
        }

        @Test
        void shouldPerformMigration() {
            List<CourtBundle> courtBundles = List.of(
                createCourtBundle("hearing 1",
                    "doc1", "url", "binaryUrl"),
                createCourtBundle("hearing 2",
                    "doc2", "url2", "binaryUrl2"),
                createCourtBundle("hearing 1",
                    "doc3", "url3", "binaryUrl3")
            );

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
                        .courtBundle(wrapElements(List.of(
                            createCourtBundle("hearing 1",
                                "doc1", "url", "binaryUrl"),
                            createCourtBundle("hearing 1",
                                "doc3", "url3", "binaryUrl3"))))
                        .build(),
                    HearingCourtBundle.builder()
                        .hearing("hearing 2")
                        .courtBundle(wrapElements(List.of(
                            createCourtBundle("hearing 2",
                                "doc2", "url2", "binaryUrl2"))))
                        .build()

                );
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

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl465 {
        private final String migrationId = "DFPL-465";
        private final long validCaseId = 1639997900244470L;
        private final long invalidCaseId = 1626258358022000L;

        @Test
        void shouldPerformMigrationWhenNameMatches() {
            CaseData caseData = CaseData.builder()
                    .id(validCaseId)
                    .state(State.SUBMITTED)
                    .legalRepresentatives(
                        wrapElements(
                            LegalRepresentative.builder()
                                    .fullName("Stacey Halbert")
                                    .email("first@gamil.com")
                                    .build(),
                            LegalRepresentative.builder()
                                    .fullName("Della Phillips")
                                    .email("second@gamil.com")
                                    .build(),
                            LegalRepresentative.builder()
                                    .fullName("Natalie Beardsmore")
                                    .email("first@gamil.com")
                                    .build(),
                            LegalRepresentative.builder()
                                    .fullName("Donna Bird")
                                    .email("second@gamil.com")
                                    .build(),
                            LegalRepresentative.builder()
                                    .fullName("First User")
                                    .email("first@gamil.com")
                                    .build(),
                            LegalRepresentative.builder()
                                    .fullName("Second User")
                                    .email("second@gamil.com")
                                    .build()
                        )
                    )
                    .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                    buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getLegalRepresentatives()).hasSize(2);
            List<LegalRepresentative> legalRepresentatives = unwrapElements(responseData.getLegalRepresentatives());

            assertThat(legalRepresentatives).extracting("fullName")
                    .contains("First User", "Second User");
        }

        @Test
        void shouldThrowAssersionErrorWhenCaseIdIsInvalid() {
            CaseData caseData = CaseData.builder()
                .id(invalidCaseId)
                .state(State.SUBMITTED)
                    .legalRepresentatives(
                        wrapElements(
                            LegalRepresentative.builder()
                                    .fullName("First User")
                                    .email("first@gamil.com")
                                    .build(),
                            LegalRepresentative.builder()
                                    .fullName("Second User")
                                    .email("second@gamil.com")
                                    .build()
                        )
                    )
                    .build();
            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-465, case reference = 1626258358022000},"
                    + " expected case id 1639997900244470");
        }
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }
}
