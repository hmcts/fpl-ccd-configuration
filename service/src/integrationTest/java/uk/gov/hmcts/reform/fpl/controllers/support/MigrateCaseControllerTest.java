package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    public static final DocumentReference DOCUMENT_REFERENCE = testDocumentReference();


    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    private static final String INVALID_MIGRATION_ID = "invalid id";

    @MockBean
    private TaskListService taskListService;

    @MockBean
    private TaskListRenderer taskListRenderer;

    @MockBean
    private CaseSubmissionChecker caseSubmissionChecker;

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
    class Dfpl500 {
        private final String migrationId = "DFPL-500";
        private final long validCaseId = 1643728359576136L;
        private final long invalidCaseId = 1626258358022000L;

        @Test
        void shouldPerformMigrationWhenNameMatches() {
            List<UUID> uuidsToBeRetained = List.of(UUID.randomUUID(), UUID.randomUUID());
            CaseData caseData = CaseData.builder()
                    .id(validCaseId)
                    .state(State.SUBMITTED)
                    .documentsSentToParties(
                        wrapElements(
                        SentDocuments.builder()
                            .documentsSentToParty(
                                List.of(
                                    element(
                                        uuidsToBeRetained.get(0),
                                        SentDocument.builder()
                                            .document(
                                                DocumentReference.builder()
                                                    .filename("ToBeRetained.doc")
                                                    .build()
                                            ).build()
                                    ),
                                    element(
                                        UUID.fromString("ad5c738e-d7aa-4ccf-b53b-0b1e40a19182"),
                                        SentDocument.builder()
                                            .document(
                                                DocumentReference.builder()
                                                    .filename("ToBeRemoved.doc")
                                                    .build()
                                            ).build()
                                    )
                                )
                            ).build(),
                        SentDocuments.builder()
                            .documentsSentToParty(
                                List.of(
                                    element(
                                        uuidsToBeRetained.get(1),
                                        SentDocument.builder()
                                            .document(
                                                DocumentReference.builder()
                                                    .filename("ToBeRetained.doc")
                                                    .build()
                                            ).build()
                                    ),
                                    element(
                                        UUID.fromString("61f97374-360b-4759-9329-af10fae1317e"),
                                        SentDocument.builder()
                                            .document(
                                                DocumentReference.builder()
                                                    .filename("ToBeRemoved.doc")
                                                    .build()
                                            ).build()
                                    )
                                )
                            ).build()
                        )
                    ).build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                    buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            List<Element<SentDocuments>> documentsSentToParties = responseData.getDocumentsSentToParties();

            List<UUID> retainedUUIDs = documentsSentToParties.stream()
                    .map(Element::getValue)
                    .flatMap(value -> value.getDocumentsSentToParty().stream())
                    .map(Element::getId)
                    .collect(toList());

            assertThat(retainedUUIDs).isEqualTo(uuidsToBeRetained);
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
                .hasMessage("Migration {id = DFPL-500, case reference = 1626258358022000},"
                    + " expected case id 1643728359576136");
        }
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }
}
