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

import java.util.List;
import java.util.Map;
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
    class Dfpl2818 {
        @Test
        void shouldBlankLegacyDocumentViewFieldsIfPresent() {
            CaseData caseData = CaseData.builder().build();
            CaseDetails caseDetails = buildCaseDetails(caseData, "DFPL-2818");
            caseDetails.getData().put("documentViewHMCTS", "someValue");
            caseDetails.getData().put("documentViewLA", "anotherValue");
            caseDetails.getData().put("documentViewNC", "thirdValue");

            Map<String, Object> data = postAboutToSubmitEvent(caseDetails).getData();
            assertThat(data.get("documentViewHMCTS")).isEqualTo("");
            assertThat(data.get("documentViewLA")).isEqualTo("");
            assertThat(data.get("documentViewNC")).isEqualTo("");
        }

        @Test
        void shouldNotFailIfFieldsAreAbsent() {
            CaseData caseData = CaseData.builder().build();
            CaseDetails caseDetails = buildCaseDetails(caseData, "DFPL-2818");

            Map<String, Object> data = postAboutToSubmitEvent(caseDetails).getData();
            assertThat(data.containsKey("documentViewHMCTS")).isFalse();
            assertThat(data.containsKey("documentViewLA")).isFalse();
            assertThat(data.containsKey("documentViewNC")).isFalse();
        }
    }
}
