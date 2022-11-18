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
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
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
    class DfplRemoveCaseNotes {
        private Stream<Arguments> provideMigrationTestData() {
            return Stream.of(
                Arguments.of("DFPL-810", 1639481593478877L,
                    List.of(UUID.fromString("2824e43b-3250-485a-b069-6fd06390ce83")))
            );
        }

        @ParameterizedTest
        @MethodSource("provideMigrationTestData")
        void shouldRemoveCaseNotes(String migrationId, Long expectedCaseId, List<UUID> validNoteId) {
            UUID otherNoteId = UUID.randomUUID();
            List<UUID> testNoteId = new ArrayList<>(validNoteId);
            testNoteId.add(otherNoteId);

            CaseData caseData = CaseData.builder()
                .id(expectedCaseId)
                .caseNotes(testNoteId.stream().map(this::buildMockCaseNotes).collect(Collectors.toList()))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getCaseNotes().size()).isEqualTo(1);
            assertThat(responseData.getCaseNotes().stream().map(Element::getId).collect(Collectors.toList()))
                .doesNotContainAnyElementsOf(validNoteId)
                .contains(otherNoteId);

        }

        @ParameterizedTest
        @MethodSource("provideMigrationTestData")
        void shouldThrowExceptionWhenCaseIdInvalid(String migrationId, Long expectedCaseId, List<UUID> validNoteId) {
            CaseData caseData = CaseData.builder().id(1L).build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = %s}, expected case id %d",
                    migrationId, 1, expectedCaseId));
        }

        @ParameterizedTest
        @MethodSource("provideMigrationTestData")
        void shouldThrowExceptionWhenCasNotesIdInvalid(String migrationId, Long expectedCaseId,
                                                       List<UUID> validNoteId) {
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
    class Dfpl1001 {
        final String migrationId = "DFPL-1001";
        final Long caseId = 1649335087796806L;
        final UUID hearingId = UUID.fromString("9cc3f847-3f2c-4d19-bf32-ed1377866ffe");

        final Element<HearingBooking> hearingToBeRemoved = element(hearingId, HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(now().minusDays(3))
            .endDate(now().minusDays(2))
            .build());
        final Element<HearingBooking> hearingBooking1 = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(now().minusDays(3))
            .endDate(now().minusDays(2))
            .build());
        final Element<HearingBooking> hearingBooking2 = element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(now().minusDays(3))
            .endDate(now().minusDays(2))
            .build());

        @Test
        void shouldPerformMigrationWhenHearingIdMatches() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .hearingDetails(List.of(hearingToBeRemoved, hearingBooking1, hearingBooking2))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            List<Element<HearingBooking>> expectedHearingDetails = List.of(hearingBooking1, hearingBooking2);

            assertThat(responseData.getHearingDetails()).isEqualTo(expectedHearingDetails);
            assertThat(responseData.getSelectedHearingId()).isIn(hearingBooking1.getId(), hearingBooking2.getId());
        }

        @Test
        void shouldThrowAssertionErrorWhenHearingDetailsIsNull() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = %s},"
                    + " hearing details not found", migrationId, caseId));
        }

        @Test
        void shouldThrowAssertionErrorWhenCaseIdIsInvalid() {
            CaseData caseData = CaseData.builder()
                .id(1111111111111111L)
                .hearingDetails(List.of(hearingToBeRemoved, hearingBooking1, hearingBooking2))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = 1111111111111111},"
                    + " expected case id %s", migrationId, caseId));
        }

        @Test
        void shouldThrowAssertionErrorWhenHearingBookingIdIsInvalid() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .hearingDetails(List.of(hearingBooking1, hearingBooking2))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = %s},"
                    + " hearing booking %s not found", migrationId, caseId, hearingId));
        }

        @Test
        void shouldThrowAssertionErrorWhenMoreThanOneHearingBookingWithSameIdFound() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .hearingDetails(List.of(hearingToBeRemoved, hearingToBeRemoved))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = %s},"
                    + " more than one hearing booking %s found", migrationId, caseId, hearingId));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl828 {
        final String migrationId = "DFPL-828";
        final long expectedCaseId = 1651850415891595L;
        final UUID expectedPartyId = UUID.fromString("f3264cc6-61b7-4cf7-ab37-c9eb35a13e03");
        final List<UUID> expectedDocId = List.of("6fcc6eb4-942d-40e1-bfa6-befd70254f7f",
                "fbea9e67-8244-43b8-97c5-265da7b9b6c7", "518a9339-786c-4b68-bce9-a064616ac47f",
                "61733660-67ed-4ea7-8711-2fe80e15af68")
            .stream().map(UUID::fromString).collect(Collectors.toList());

        UUID[] otherDocIds = new UUID[]{UUID.randomUUID(), UUID.randomUUID()};
        UUID otherPartyId = UUID.randomUUID();

        Element<SentDocuments> targetDocumentSentToParties = element(expectedPartyId, SentDocuments.builder()
            .documentsSentToParty(List.of(element(expectedDocId.get(0), SentDocument.builder().build()),
                element(expectedDocId.get(1), SentDocument.builder().build()),
                element(expectedDocId.get(2), SentDocument.builder().build()),
                element(expectedDocId.get(3), SentDocument.builder().build()),
                element(otherDocIds[0], SentDocument.builder().build()),
                element(otherDocIds[1], SentDocument.builder().build())))
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
                .doesNotContainAnyElementsOf(expectedDocId)
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
                            element(otherDocIds[1], SentDocument.builder().build()))).build())))
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format(
                    "Migration {id = %s, case reference = %s}, document Id not found",
                    migrationId, expectedCaseId));
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl1012 {
        private static final long invalidCaseId = 8888888888888888L;
        private static final long validCaseId = 1661877618161045L;
        private static final String migrationId = "DFPL-1012";

        private final UUID positionStatementRespondentId = UUID.fromString("b8da3a48-441f-4210-a21c-7008d256aa32");

        private final Element<PositionStatementRespondent> positionStatementRespondent = element(
            positionStatementRespondentId, PositionStatementRespondent.builder().build());

        @Test
        void shouldRemovePositionStatementRespondent() {
            CaseData caseData = prepareValidCaseData();
            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);
            assertThat(responseData.getHearingDocuments().getPositionStatementRespondentListV2())
                .doesNotContain(positionStatementRespondent);
        }

        @Test
        void shouldThrowExceptionIfWrongCase() {
            CaseData caseData = CaseData.builder().id(invalidCaseId).build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = 8888888888888888}, expected case id %d",
                    migrationId, validCaseId));
        }

        @Test
        void shouldThrowExceptionIfWrongPositionStatementRespondent() {
            CaseData caseData = CaseData.builder().id(validCaseId)
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementRespondentListV2(List.of(
                        element(UUID.randomUUID(), PositionStatementRespondent.builder().build()))
                    )
                    .build())
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = %s}, invalid position statement "
                        + "respondent",
                    migrationId, validCaseId));
        }

        private CaseData prepareValidCaseData() {
            return CaseData.builder()
                .id(validCaseId)
                .hearingDocuments(HearingDocuments.builder()
                    .positionStatementRespondentListV2(List.of(
                        element(UUID.randomUUID(), PositionStatementRespondent.builder().build()),
                        element(UUID.randomUUID(), PositionStatementRespondent.builder().build()),
                        positionStatementRespondent
                    ))
                    .build())
                .build();
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl985 {
        private static final long invalidCaseId = 8888888888888888L;
        private static final long validCaseId = 1648203424556112L;
        private static final String migrationId = "DFPL-985";

        private final UUID respondentStatementId = UUID.fromString("4b88563e-c6b3-4780-90b6-531e1db65b7e");

        private final Element<RespondentStatement> respondentStatementElement = element(
            respondentStatementId, RespondentStatement.builder().build());

        @Test
        void shouldRemoveRespondentStatement() {
            CaseData caseData = prepareValidCaseData();
            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);
            assertThat(responseData.getRespondentStatements()).doesNotContain(respondentStatementElement);
        }

        @Test
        void shouldThrowExceptionIfWrongCase() {
            CaseData caseData = CaseData.builder().id(invalidCaseId).build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = 8888888888888888}, expected case id %d",
                    migrationId, validCaseId));
        }

        @Test
        void shouldThrowExceptionIfWrongRespondentStatement() {
            CaseData caseData = CaseData.builder().id(validCaseId)
                .respondentStatements(List.of(
                    element(UUID.randomUUID(), RespondentStatement.builder().build()))
                ).build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = %s}, invalid respondent statements",
                    migrationId, validCaseId));
        }

        private CaseData prepareValidCaseData() {
            return CaseData.builder()
                .id(validCaseId)
                .respondentStatements(List.of(
                    element(UUID.randomUUID(), RespondentStatement.builder().respondentId(UUID.randomUUID()).build()),
                    element(UUID.randomUUID(), RespondentStatement.builder().respondentId(UUID.randomUUID()).build()),
                    respondentStatementElement
                )).build();
        }
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }
}
