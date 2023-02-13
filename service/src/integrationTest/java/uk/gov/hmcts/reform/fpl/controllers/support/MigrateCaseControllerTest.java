package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.enums.HearingOptions;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testOrganisation;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList.TIMETABLE_FOR_PROCEEDINGS;

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
    class Dfpl1144 {
        final String migrationId = "DFPL-1144";
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
                .hearingOption(HearingOptions.NEW_HEARING)
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId)
            );

            CaseData responseData = extractCaseData(response);
            List<Element<Child>> expectedChildren = List.of(expectedChild1,expectedChild2);

            assertThat(responseData.getAllChildren()).isEqualTo(expectedChildren);
            assertThat(responseData.getHearingOption()).isEqualTo(HearingOptions.EDIT_PAST_HEARING);
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
