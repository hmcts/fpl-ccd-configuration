package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.RespondentStatement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOrganisation;

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
    class DfplRemoveConfidentialTab {
        private final long invalidCaseId = 1643728359986136L;

        private Stream<Arguments> provideMigrationTestData() {
            return Stream.of(
                Arguments.of("DFPL-809", 1651569615587841L)
            );
        }

        @ParameterizedTest
        @MethodSource("provideMigrationTestData")
        void shouldPerformMigrationWhenDocIdMatches(String migrationId, Long validCaseId) {

            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .documentsWithConfidentialAddress(emptyList())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getDocumentsWithConfidentialAddress())
                .isEqualTo(null);
        }

        @ParameterizedTest
        @MethodSource("provideMigrationTestData")
        void shouldThrowAssersionErrorWhenCaseIdIsInvalid(String migrationId, Long validCaseId) {

            CaseData caseData = CaseData.builder()
                .id(invalidCaseId)
                .documentsWithConfidentialAddress(emptyList())
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format("Migration {id = %s, case reference = 1643728359986136}, expected case id %d",
                    migrationId, validCaseId));
        }
    }

    // @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    // @Nested
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

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl702 {
        final String migrationId = "DFPL-702";

        @Test
        void shouldMigrateGlobalSearchRequiredFieldsWithOnboardingCourtInfoOnly() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .caseName("I AM CASE NAME")
                .children1(List.of(element(Child.builder().party(ChildParty.builder()
                    .firstName("Kate")
                    .lastName("Clark")
                    .dateOfBirth(LocalDate.of(2012, 7, 31))
                    .build()).build())))
                .respondents1(List.of(element(Respondent.builder().party(RespondentParty.builder()
                    .firstName("Ronnie")
                    .lastName("Clark")
                        .dateOfBirth(LocalDate.of(1997, 9, 7))
                    .build()).build())))
                .court(Court.builder().code("344").build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );
            Map<String, Object> caseDetails = response.getData();

            assertThat(caseDetails.get("caseNameHmctsInternal")).isEqualTo("I AM CASE NAME");

            // court code (344) is defined by application-integration-test.yaml (by LOCAL_AUTHORITY_3_USER_EMAIL)
            // epimms id is defined in courts.json by looking up court code 344
            @SuppressWarnings("unchecked")
            Map<String,  String> caseManagementLocation = (Map<String, String>)
                caseDetails.get("caseManagementLocation");
            assertThat(caseManagementLocation).containsEntry("baseLocation", "234946");
            assertThat(caseManagementLocation).containsEntry("region", "7");
            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> caseManagementCategory = (Map<String, Map<String, String>>)
                caseDetails.get("caseManagementCategory");
            assertThat(caseManagementCategory).containsKey("value");
            Map<String, String> caseManagementCategoryValue =  caseManagementCategory.get("value");
            assertThat(caseManagementCategoryValue).containsEntry("code", "FPL");
            assertThat(caseManagementCategoryValue).containsEntry("label", "Family Public Law");

            assertThat(caseManagementCategory).containsKey("list_items");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> listItems = (List<Map<String, String>>) caseManagementCategory.get("list_items");
            assertThat(listItems).contains(Map.of("code", "FPL", "label", "Family Public Law"));
        }

        @Test
        void shouldMigrateGlobalSearchRequiredFieldsWithOrdersCourt() {
            CaseData caseData = CaseData.builder()
                .id(1L)
                .caseName("I AM CASE NAME")
                .children1(List.of(element(Child.builder().party(ChildParty.builder()
                    .firstName("Kate")
                    .lastName("Clark")
                    .dateOfBirth(LocalDate.of(2012, 7, 31))
                    .build()).build())))
                .respondents1(List.of(element(Respondent.builder().party(RespondentParty.builder()
                    .firstName("Ronnie")
                    .lastName("Clark")
                    .dateOfBirth(LocalDate.of(1997, 9, 7))
                    .build()).build())))
                .orders(Orders.builder().orderType(List.of(OrderType.CHILD_ASSESSMENT_ORDER)).court("344").build())
                .court(Court.builder().code("117").build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );
            Map<String, Object> caseDetails = response.getData();

            assertThat(caseDetails.get("caseNameHmctsInternal")).isEqualTo("I AM CASE NAME");

            // court code (344) is defined by application-integration-test.yaml (by LOCAL_AUTHORITY_3_USER_EMAIL)
            // epimms id is defined in courts.json by looking up court code 344
            @SuppressWarnings("unchecked")
            Map<String,  String> caseManagementLocation = (Map<String, String>)
                caseDetails.get("caseManagementLocation");
            assertThat(caseManagementLocation).containsEntry("baseLocation", "234946");
            assertThat(caseManagementLocation).containsEntry("region", "7");
            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> caseManagementCategory = (Map<String, Map<String, String>>)
                caseDetails.get("caseManagementCategory");
            assertThat(caseManagementCategory).containsKey("value");
            Map<String, String> caseManagementCategoryValue =  caseManagementCategory.get("value");
            assertThat(caseManagementCategoryValue).containsEntry("code", "FPL");
            assertThat(caseManagementCategoryValue).containsEntry("label", "Family Public Law");

            assertThat(caseManagementCategory).containsKey("list_items");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> listItems = (List<Map<String, String>>) caseManagementCategory.get("list_items");
            assertThat(listItems).contains(Map.of("code", "FPL", "label", "Family Public Law"));
        }

        @Test
        void shouldInvokeSubmitSupplementaryData() {
            final Organisation organisation = testOrganisation();

            final CaseData caseData = CaseData.builder()
                .id(nextLong())
                .state(OPEN)
                .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
                .outsourcingPolicy(organisationPolicy(
                    organisation.getOrganisationIdentifier(), organisation.getName(), LASOLICITOR))
                .build();

            postSubmittedEvent(
                buildCaseDetails(caseData, migrationId));

            Map<String, Map<String, Map<String, Object>>> supplementaryData = new HashMap<>();
            supplementaryData.put("supplementary_data_updates",
                Map.of("$set", Map.of("HMCTSServiceId", "ABA3")));

            verify(coreCaseDataApi).submitSupplementaryData(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN,
                caseData.getId().toString(), supplementaryData);
        }
    }
}
