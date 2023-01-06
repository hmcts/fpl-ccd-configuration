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
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList.TIMETABLE_FOR_PROCEEDINGS;
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
    class Dfpl1064 {
        private final long invalidCaseId = 1643728359986136L;
        private final long validCaseId = 1659605693892067L;
        private final String migrationId = "DFPL-1064";

        @Test
        void shouldThrowExceptionIfWrongCaseId() {
            CaseData caseData = CaseData.builder()
                .id(invalidCaseId)
                .sendToCtsc(YesNo.YES.getValue())
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format(
                    "Migration {id = %s, case reference = %s}, case id not present in allowed list",
                    migrationId, invalidCaseId));
        }

        @Test
        void shouldSetSendToCtscToNo() {
            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .sendToCtsc(YesNo.YES.getValue())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getSendToCtsc()).isEqualTo(YesNo.NO.getValue());
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl1065 {
        private final long invalidCaseId = 1643728359986136L;
        private final long validCaseId = 1667558394262009L;
        private final String migrationId = "DFPL-1065";

        @Test
        void shouldThrowExceptionIfWrongCaseId() {
            CaseData caseData = CaseData.builder()
                .id(invalidCaseId)
                .sendToCtsc(YesNo.YES.getValue())
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format(
                    "Migration {id = %s, case reference = %s}, case id not present in allowed list",
                    migrationId, invalidCaseId));
        }

        @Test
        void shouldSetSendToCtscToYes() {
            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .sendToCtsc(YesNo.NO.getValue())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getSendToCtsc()).isEqualTo(YesNo.YES.getValue());
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
