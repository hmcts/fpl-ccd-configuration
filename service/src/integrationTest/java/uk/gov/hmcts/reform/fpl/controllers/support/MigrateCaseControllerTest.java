package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

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
    class DfplRemoveC110a {
        private final long invalidCaseId = 1643728359576136L;
        private final UUID invalidDocId = UUID.randomUUID();

        private Stream<Arguments> provideMigrationTestData() {
            return Stream.of(
                Arguments.of("DFPL-794", 1657104996768754L, UUID.fromString("5da7ae0a-0d53-427f-a538-2cb8c9ea82b6")),
                Arguments.of("DFPL-797", 1657816793771026L, UUID.fromString("2cfd676a-665b-4d15-ae9e-5ad2930f75cb")),
                Arguments.of("DFPL-798", 1654765774567742L, UUID.fromString("1756656b-6931-467e-8dfe-ac9f152351fe")),
                Arguments.of("DFPL-802", 1659528630126722L, UUID.fromString("dcd016c6-a0de-4ed2-91ce-5582a6acaf25"))
            );
        }

        @ParameterizedTest
        @MethodSource("provideMigrationTestData")
        void shouldPerformMigrationWhenDocIdMatches(String migrationId, Long validCaseId, UUID validDocId) {

            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .c110A(C110A.builder()
                    .submittedForm(DocumentReference.builder()
                        .url(String.format("http://test.com/%s", validDocId))
                        .build())
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getC110A().getSubmittedForm()).isNull();
        }

        @ParameterizedTest
        @MethodSource("provideMigrationTestData")
        void shouldThrowAssersionErrorWhenCaseIdIsInvalid(String migrationId, Long validCaseId, UUID validDocId) {
            CaseData caseData = CaseData.builder()
                .id(invalidCaseId)
                .state(State.SUBMITTED)
                .c110A(C110A.builder()
                    .submittedForm(DocumentReference.builder()
                        .url(String.format("http://test.com/%s", validDocId))
                        .build())
                    .build())
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = 1643728359576136}, expected case id %d",
                    migrationId, validCaseId));
        }

        @ParameterizedTest
        @MethodSource("provideMigrationTestData")
        void shouldThrowAssersionErrorWhenDocumentIdIsInvalid(String migrationId, Long validCaseId, UUID validDocId) {
            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .state(State.SUBMITTED)
                .c110A(C110A.builder()
                    .submittedForm(DocumentReference.builder()
                        .url(String.format("http://test.com/%s", invalidDocId))
                        .build())
                    .build())
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = %d}, expected c110a document id %s",
                    migrationId, validCaseId, validDocId));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl692 {
        final String migrationId = "DFPL-692";
        final long expectedCaseId = 1641905747009846L;
        final UUID expectedNotesIdOne = UUID.fromString("7dd3c2ac-d49f-4119-8299-a19a62f1d6db");
        final UUID expectedNotesIdTwo = UUID.fromString("66fb7c25-7860-4a5c-98d4-dd2ff575eb28");

        @Test
        void shouldRemoveNotes() {
            UUID otherNoteId = UUID.randomUUID();

            CaseData caseData = CaseData.builder()
                .id(expectedCaseId)
                .caseNotes(List.of(buildMockCaseNotes(expectedNotesIdOne),
                    buildMockCaseNotes(expectedNotesIdTwo),
                    buildMockCaseNotes(otherNoteId)))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getCaseNotes().size()).isEqualTo(1);
            assertThat(responseData.getCaseNotes().stream().map(Element::getId).collect(Collectors.toList()))
                .doesNotContain(expectedNotesIdOne, expectedNotesIdTwo)
                .contains(otherNoteId);

        }

        @Test
        void shouldThrowExceptionWhenCaseIdInvalid() {
            CaseData caseData = CaseData.builder().id(1L).build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = %s}, expected case id %d",
                    migrationId, 1, expectedCaseId));
        }

        @Test
        void shouldThrowExceptionWhenCasNotesIdInvalid() {
            CaseData caseData = CaseData.builder()
                .id(expectedCaseId)
                .caseNotes(List.of(buildMockCaseNotes(UUID.randomUUID()),
                    buildMockCaseNotes(UUID.randomUUID()),
                    buildMockCaseNotes(UUID.randomUUID()))).build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format(
                    "Migration {id = %s, case reference = %s}, expected caseNotes id not found",
                    migrationId, expectedCaseId));
        }

        @Test
        void shouldThrowExceptionWhenOneCasNotesIdInvalid() {
            CaseData caseData = CaseData.builder()
                .id(expectedCaseId)
                .caseNotes(List.of(buildMockCaseNotes(expectedNotesIdOne),
                    buildMockCaseNotes(UUID.randomUUID()),
                    buildMockCaseNotes(UUID.randomUUID()))).build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format(
                    "Migration {id = %s, case reference = %s}, expected caseNotes id not found",
                    migrationId, expectedCaseId));
        }

        private Element<CaseNote> buildMockCaseNotes(UUID id) {
            return element(id, CaseNote.builder()
                .createdBy("mockCreatedBy")
                .date(LocalDate.of(2022, 6, 14))
                .note("Testing Note")
                .build());
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl776 {
        final String migrationId = "DFPL-776";
        final long expectedCaseId = 1646318196381762L;
        final UUID expectedMsgId = UUID.fromString("878a2dd7-8d50-46b1-88d3-a5c6fe9a39ba");

        @Test
        void shouldRemoveMsg() {
            UUID otherMsgId = UUID.randomUUID();

            CaseData caseData = CaseData.builder()
                .id(expectedCaseId)
                .judicialMessages(List.of(
                    element(otherMsgId, JudicialMessage.builder()
                        .dateSent("19 May 2022 at 10:16am")
                        .latestMessage("Test Message").build()),
                    element(expectedMsgId, JudicialMessage.builder()
                        .dateSent("19 May 2022 at 11:16am")
                        .latestMessage("Test Message to be removed").build())
                ))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getJudicialMessages().size()).isEqualTo(1);
            assertThat(responseData.getJudicialMessages().stream().map(Element::getId).collect(Collectors.toList()))
                .doesNotContain(expectedMsgId)
                .contains(otherMsgId);
        }

        @Test
        void shouldThrowExceptionWhenCaseIdInvalid() {
            CaseData caseData = CaseData.builder().id(1L).build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = %s}, expected case id %d",
                    migrationId, 1, expectedCaseId));
        }

        @Test
        void shouldThrowExceptionWhenMsgIdInvalid() {
            CaseData caseData = CaseData.builder()
                .id(expectedCaseId)
                .judicialMessages(wrapElements(
                    JudicialMessage.builder()
                        .dateSent("19 May 2022 at 10:16am")
                        .latestMessage("Test Message").build()))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format(
                    "Migration {id = %s, case reference = %s}, invalid JudicialMessage ID",
                    migrationId, expectedCaseId));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl816 {
        final String migrationId = "DFPL-816";
        final long expectedCaseId = 1659614144221728L;
        final UUID expectedPartyId = UUID.fromString("a7549435-47ca-4c2c-aaec-7ddd81befc1d");
        final UUID expectedDocId = UUID.fromString("d4779e5e-1f3a-4f21-967d-7b6109931009");

        UUID[] otherDocIds = new UUID[] {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};
        UUID otherPartyId = UUID.randomUUID();

        Element<SentDocuments> targetDocumentSentToParties = element(expectedPartyId, SentDocuments.builder()
            .documentsSentToParty(List.of(element(expectedDocId, SentDocument.builder().build()),
                element(otherDocIds[0], SentDocument.builder().build()),
                element(otherDocIds[1], SentDocument.builder().build()),
                element(otherDocIds[2], SentDocument.builder().build()),
                element(otherDocIds[3], SentDocument.builder().build()),
                element(otherDocIds[4], SentDocument.builder().build()),
                element(otherDocIds[5], SentDocument.builder().build())))
            .build());

        Element<SentDocuments> otherDocumentSentToParties = element(otherPartyId, SentDocuments.builder()
            .documentsSentToParty(List.of(element(SentDocument.builder().build()),
                element(SentDocument.builder().build()))).build());

        @Test
        void shouldRemoveDocumentLinked() {

            CaseData caseData = CaseData.builder()
                .id(expectedCaseId)
                .documentsSentToParties(List.of(targetDocumentSentToParties, otherDocumentSentToParties))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getDocumentsSentToParties().size()).isEqualTo(2);
            assertThat(responseData.getDocumentsSentToParties().stream()
                .map(Element::getId).collect(Collectors.toList()))
                .containsExactly(expectedPartyId, otherPartyId);

            assertThat(ElementUtils.getElement(expectedPartyId, responseData.getDocumentsSentToParties()).getValue()
                .getDocumentsSentToParty().stream().map(Element::getId).collect(Collectors.toList()))
                .doesNotContain(expectedDocId)
                .containsExactly(otherDocIds);
        }

        @Test
        void shouldThrowExceptionWhenCaseIdInvalid() {
            CaseData caseData = CaseData.builder().id(1L).build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = %s}, expected case id %d",
                    migrationId, 1, expectedCaseId));
        }

        @Test
        void shouldThrowExceptionWhenPartyIdInvalid() {
            CaseData caseData = CaseData.builder()
                .id(expectedCaseId)
                .documentsSentToParties(List.of(otherDocumentSentToParties))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format(
                    "Migration {id = %s, case reference = %s}, party Id not found",
                    migrationId, expectedCaseId));
        }

        @Test
        void shouldThrowExceptionWhenDocIdInvalid() {
            CaseData caseData = CaseData.builder()
                .id(expectedCaseId)
                .documentsSentToParties(List.of(element(expectedPartyId, SentDocuments.builder()
                        .documentsSentToParty(List.of(
                            element(otherDocIds[0], SentDocument.builder().build()),
                            element(otherDocIds[1], SentDocument.builder().build()),
                            element(otherDocIds[2], SentDocument.builder().build()),
                            element(otherDocIds[3], SentDocument.builder().build()),
                            element(otherDocIds[4], SentDocument.builder().build()),
                            element(otherDocIds[5], SentDocument.builder().build()))).build())))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format(
                    "Migration {id = %s, case reference = %s}, document Id not found",
                    migrationId, expectedCaseId));
        }
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }
}
