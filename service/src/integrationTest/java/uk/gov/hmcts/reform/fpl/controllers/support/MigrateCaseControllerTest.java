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
    class Dfpl2713 {

        @Test
        void shouldRedactStrings() {
            CaseData caseData = CaseData.builder()
                .id(1734095429043780L)
                .changeOfRepresentatives(List.of(
                    element(UUID.randomUUID(), ChangeOfRepresentation.builder()
                        .child("unchanged name")
                        .build()),
                    element(UUID.fromString("673396a8-dcba-451e-a4df-5a2162ac2828"),
                        ChangeOfRepresentation.builder()
                            .child("aaaaaaa bbbbbb")
                            .build()),
                    element(UUID.fromString("64e99c83-6eb3-48f7-8ba6-2de983af1a8d"),
                        ChangeOfRepresentation.builder()
                            .child("ccccccccc ddd")
                            .build())
                ))
                .build();

            CaseData after = extractCaseData(postAboutToSubmitEvent(buildCaseDetails(caseData, "DFPL-2713")));

            assertThat(after.getChangeOfRepresentatives()).hasSize(3);
            assertThat(after.getChangeOfRepresentatives().stream()
                .map(Element::getValue)
                .map(ChangeOfRepresentation::getChild))
                .containsExactly("unchanged name", "aaaaaaa", "ccccccccc");
            ;
        }

    }
}
