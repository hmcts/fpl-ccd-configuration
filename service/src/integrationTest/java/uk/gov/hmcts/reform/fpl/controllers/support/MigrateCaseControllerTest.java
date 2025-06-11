package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
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

    private static final String INVALID_MIGRATION_ID = "invalid id";

    @Test
    void shouldThrowExceptionWhenMigrationNotMappedForMigrationID() {
        CaseData caseData = CaseData.builder().build();

        assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, INVALID_MIGRATION_ID)))
            .getRootCause()
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No migration mapped to " + INVALID_MIGRATION_ID);
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }

    @BeforeEach
    void setup() {
        givenSystemUser();
        givenFplService();
    }

    @Nested
    class Dfpl2740 {

        @Test
        void shouldRedactStrings() {
            CaseData caseData = CaseData.builder()
                .id(1743167066103323L)
                .changeOfRepresentatives(List.of(
                    element(UUID.randomUUID(), ChangeOfRepresentation.builder()
                        .child("unchanged name")
                        .build()),
                    element(UUID.fromString("625f113c-5673-4b35-bbf1-6507fcf9ec43"),
                        ChangeOfRepresentation.builder()
                            .child("AAAAA BBBB")
                            .build())
                ))
                .build();

            CaseData after = extractCaseData(postAboutToSubmitEvent(buildCaseDetails(caseData, "DFPL-2740")));

            assertThat(after.getChangeOfRepresentatives()).hasSize(2);
            assertThat(after.getChangeOfRepresentatives().stream()
                .map(Element::getValue)
                .map(ChangeOfRepresentation::getChild))
                .containsExactly("unchanged name", "AAAAA");
            ;
        }

    }

    @Nested
    class Dfpl2677 {
        private static final LocalDate NOW = LocalDate.now();

        @Test
        void shouldMigrateCase() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .dateSubmitted(NOW)
                .build();

            CaseData after = extractCaseData(postAboutToSubmitEvent(buildCaseDetails(caseData,
                "DFPL-2677")));

            assertThat(after.getDateSubmitted()).isNull();
            assertThat(after.getLastSubmittedDate()).isEqualTo(NOW);
        }

        @Test
        void shouldNotMigrateCaseIfInvalid() {
            assertThatThrownBy(() ->
                postAboutToSubmitEvent(buildCaseDetails(CaseData.builder()
                    .id(1L)
                    .build(),
                "DFPL-2677")))
                .hasMessageContaining("[Case 1], dateSubmitted is null or lastSubmittedDate is not null");

            assertThatThrownBy(() ->
                postAboutToSubmitEvent(buildCaseDetails(CaseData.builder()
                        .id(1L)
                        .dateSubmitted(NOW)
                        .lastSubmittedDate(NOW)
                        .build(),
                    "DFPL-2677")))
                .hasMessageContaining("[Case 1], dateSubmitted is null or lastSubmittedDate is not null");
        }

        @Test
        void shouldRollbackCase() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .lastSubmittedDate(NOW)
                .build();

            CaseData after = extractCaseData(postAboutToSubmitEvent(buildCaseDetails(caseData,
                "DFPL-2677-rollback")));

            assertThat(after.getLastSubmittedDate()).isNull();
            assertThat(after.getDateSubmitted()).isEqualTo(NOW);
        }

        @Test
        void shouldNotRollbackCaseIfInvalid() {
            assertThatThrownBy(() ->
                postAboutToSubmitEvent(buildCaseDetails(CaseData.builder()
                        .id(1L)
                        .build(),
                    "DFPL-2677-rollback")))
                .hasMessageContaining("[Case 1], lastSubmittedDate is null or dateSubmitted is not null");

            assertThatThrownBy(() ->
                postAboutToSubmitEvent(buildCaseDetails(CaseData.builder()
                        .id(1L)
                        .dateSubmitted(NOW)
                        .lastSubmittedDate(NOW)
                        .build(),
                    "DFPL-2677-rollback")))
                .hasMessageContaining("[Case 1], lastSubmittedDate is null or dateSubmitted is not null");
        }
    }
}
