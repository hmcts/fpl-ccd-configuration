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
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList.TIMETABLE_FOR_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

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
    class DfplRemoveCaseNotes {
        private Stream<Arguments> provideMigrationTestData() {
            return Stream.of(
                Arguments.of("DFPL-979", 1648556593632182L,
                    List.of(UUID.fromString("c0c0c620-055e-488c-a6a9-d5e7ec35c210"),
                        UUID.fromString("0a202483-b7e6-44a1-a28b-8c9342f67967")))
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
    class Dfpl1015 {
        final String migrationId = "DFPL-1015";
        final Long caseId = 1641373238062313L;
        final UUID hearingId = UUID.fromString("894fa026-e403-45e8-a2fe-105e8135ee5b");

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
    class Dfpl1029 {

        final String migrationId = "DFPL-1029";
        final long expectedCaseId = 1638876373455956L;
        final long incorrectCaseId = 111111111111111L;

        @Test
        void shouldThrowExceptionWhenIncorrectCaseId() {
            CaseData caseData = CaseData.builder().id(incorrectCaseId).build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = %s}, expected case id %d",
                    migrationId, incorrectCaseId, expectedCaseId));
        }

        @Test
        void shouldSetCaseStateToCaseManagement() {
            CaseData caseData = CaseData.builder().id(expectedCaseId).build();

            CaseDetails caseDetails = buildCaseDetails(caseData, migrationId);

            // pick a few of the temp fields from ManageOrderDocumentScopedFieldsCalculator and set on CaseDetails
            caseDetails.getData().put("others_label", "test");
            caseDetails.getData().put("appointedGuardians_label", "test");
            caseDetails.getData().put("manageOrdersCafcassRegion", "test");

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

            // check that the migration has successfully removed them
            assertThat(response.getData()).extracting("others_label").isNull();
            assertThat(response.getData()).extracting("appointedGuardians_label").isNull();
            assertThat(response.getData()).extracting("manageOrdersCafcassRegion").isNull();
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

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl872 {
        final String migrationId = "DFPL-872";
        final LocalDate extensionDate = LocalDate.now();
        final Long caseId = 1660300177298257L;
        final UUID child1Id = UUID.fromString("d76c0df0-2fe3-4ee7-aafa-3703bdc5b7e0");
        final UUID child2Id = UUID.fromString("c76c0df0-2fe3-4ee7-aafa-3703bdc5b7e0");
        final Element<Child> childToBeUpdated1 = element(child1Id, Child.builder()
            .party(ChildParty.builder()
                .firstName("Jim")
                .lastName("Bob")
                .build())
            .build());
        final Element<Child> childToBeUpdated2 = element(child2Id, Child.builder()
            .party(ChildParty.builder()
                .firstName("Fred")
                .lastName("Frederson")
                .build())
            .build());
        final Element<Child> expectedChild1 = element(child1Id, Child.builder()
            .party(ChildParty.builder()
                .firstName("Jim")
                .lastName("Bob")
                .completionDate(extensionDate)
                .extensionReason(TIMETABLE_FOR_PROCEEDINGS)
                .build())
            .build());
        final Element<Child> expectedChild2 = element(child2Id, Child.builder()
            .party(ChildParty.builder()
                .firstName("Fred")
                .lastName("Frederson")
                .completionDate(extensionDate)
                .extensionReason(TIMETABLE_FOR_PROCEEDINGS)
                .build())
            .build());

        @Test
        void shouldAddAdditionalFieldsToChildren() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .state(State.CASE_MANAGEMENT)
                .caseCompletionDate(extensionDate)
                .caseExtensionReasonList(TIMETABLE_FOR_PROCEEDINGS)
                .children1(List.of(childToBeUpdated1, childToBeUpdated2))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);
            List<Element<Child>> expectedChildren = List.of(expectedChild1,expectedChild2);

            assertThat(responseData.getAllChildren()).isEqualTo(expectedChildren);
        }

        @Test
        void shouldNotUpdateWhenNoExtensionPresent() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .state(State.CASE_MANAGEMENT)
                .children1(List.of(childToBeUpdated1, childToBeUpdated2))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);
            List<Element<Child>> unchangedChildren = List.of(childToBeUpdated1, childToBeUpdated2);

            assertThat(responseData.getAllChildren()).isEqualTo(unchangedChildren);
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl872Rollback {
        final String migrationId = "DFPL-872rollback";
        final LocalDate extensionDate = LocalDate.now();
        final Long caseId = 1660300177298257L;
        final UUID child1Id = UUID.fromString("d76c0df0-2fe3-4ee7-aafa-3703bdc5b7e0");
        final UUID child2Id = UUID.fromString("c76c0df0-2fe3-4ee7-aafa-3703bdc5b7e0");
        final Element<Child> childToBeRolledBack1 = element(child1Id, Child.builder()
            .party(ChildParty.builder()
                .firstName("Jim")
                .lastName("Bob")
                .completionDate(extensionDate)
                .extensionReason(TIMETABLE_FOR_PROCEEDINGS)
                .build())
            .build());
        final Element<Child> childToBeRolledBack2 = element(child2Id, Child.builder()
            .party(ChildParty.builder()
                .firstName("Fred")
                .lastName("Frederson")
                .completionDate(extensionDate)
                .extensionReason(TIMETABLE_FOR_PROCEEDINGS)
                .build())
            .build());
        final Element<Child> expectedChild1 = element(child1Id, Child.builder()
            .party(ChildParty.builder()
                .firstName("Jim")
                .lastName("Bob")
                .completionDate(null)
                .extensionReason(null)
                .build())
            .build());
        final Element<Child> expectedChild2 = element(child2Id, Child.builder()
            .party(ChildParty.builder()
                .firstName("Fred")
                .lastName("Frederson")
                .completionDate(null)
                .extensionReason(null)
                .build())
            .build());

        @Test
        void shouldRemoveNewExtensionFields() {
            CaseData caseData = CaseData.builder()
                .id(caseId)
                .state(State.CASE_MANAGEMENT)
                .caseCompletionDate(extensionDate)
                .caseExtensionReasonList(TIMETABLE_FOR_PROCEEDINGS)
                .children1(List.of(childToBeRolledBack1, childToBeRolledBack2))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);
            List<Element<Child>> expectedChildren = List.of(expectedChild1, expectedChild2);

            assertThat(responseData.getAllChildren()).isEqualTo(expectedChildren);
        }
    }

    private CaseDetails buildCaseDetails(CaseData caseData, String migrationId) {
        CaseDetails caseDetails = asCaseDetails(caseData);
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }
}
