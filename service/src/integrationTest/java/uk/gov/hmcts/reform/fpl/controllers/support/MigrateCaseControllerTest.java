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
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;
import uk.gov.hmcts.reform.rd.model.Organisation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.State.OPEN;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_CODE;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_NAME;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_REGION;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
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

    //@Test
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

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl702 {
        final String migrationId = "DFPL-702";

        //@Test
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
            Map<String, String> caseManagementLocation = (Map<String, String>)
                caseDetails.get("caseManagementLocation");
            assertThat(caseManagementLocation).containsEntry("baseLocation", "234946");
            assertThat(caseManagementLocation).containsEntry("region", "7");
            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> caseManagementCategory = (Map<String, Map<String, String>>)
                caseDetails.get("caseManagementCategory");
            assertThat(caseManagementCategory).containsKey("value");
            Map<String, String> caseManagementCategoryValue = caseManagementCategory.get("value");
            assertThat(caseManagementCategoryValue).containsEntry("code", "FPL");
            assertThat(caseManagementCategoryValue).containsEntry("label", "Family Public Law");

            assertThat(caseManagementCategory).containsKey("list_items");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> listItems = (List<Map<String, String>>) caseManagementCategory.get("list_items");
            assertThat(listItems).contains(Map.of("code", "FPL", "label", "Family Public Law"));
        }

        //@Test
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
            Map<String, String> caseManagementLocation = (Map<String, String>)
                caseDetails.get("caseManagementLocation");
            assertThat(caseManagementLocation).containsEntry("baseLocation", "234946");
            assertThat(caseManagementLocation).containsEntry("region", "7");
            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> caseManagementCategory = (Map<String, Map<String, String>>)
                caseDetails.get("caseManagementCategory");
            assertThat(caseManagementCategory).containsKey("value");
            Map<String, String> caseManagementCategoryValue = caseManagementCategory.get("value");
            assertThat(caseManagementCategoryValue).containsEntry("code", "FPL");
            assertThat(caseManagementCategoryValue).containsEntry("label", "Family Public Law");

            assertThat(caseManagementCategory).containsKey("list_items");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> listItems = (List<Map<String, String>>) caseManagementCategory.get("list_items");
            assertThat(listItems).contains(Map.of("code", "FPL", "label", "Family Public Law"));
        }

        //@Test
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

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Dfpl1401 {

        private final String migrationId = "DFPL-1401";
        private final long validCaseId = 1666959378667166L;
        private final long invalidCaseId = 1643728359986136L;

        //@Test
        void shouldAddRelatingLA() {
            CaseData caseData = CaseData.builder()
                .id(validCaseId)
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getRelatingLA()).isEqualTo("NCC");
        }

        //@Test
        void shouldThrowExceptionIfWrongCaseId() {
            CaseData caseData = CaseData.builder()
                .id(invalidCaseId)
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage(String.format(
                    "Migration {id = %s, case reference = %s}, case id not one of the expected options",
                    migrationId, invalidCaseId));
        }
    }

    @Nested
    class Dfpl1352 {

        private final String migrationId = "DFPL-1352";

        //@Test
        void shouldThrowExceptionWhenInHighCourt() {
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .court(new Court(RCJ_HIGH_COURT_NAME, "highcourt@email.com", RCJ_HIGH_COURT_CODE,
                    RCJ_HIGH_COURT_REGION, null, null, null))
                .sendToCtsc("No")
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-1352, case reference = 12345}, "
                    + "Skipping migration as case is in the High Court");
        }

        //@Test
        void shouldThrowExceptionWhenAlreadySendingToCtsc() {
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .court(new Court("Name", "court@email.com", "001", "Region", null, null, null))
                .sendToCtsc("Yes")
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(buildCaseDetails(caseData, migrationId)))
                .getRootCause()
                .isInstanceOf(AssertionError.class)
                .hasMessage("Migration {id = DFPL-1352, case reference = 12345}, "
                    + "Skipping migration as case is already sending to the CTSC");
        }

        //@Test
        void shouldMigrateCaseIfInNormalCourtAndNotSendingToCtsc() {
            CaseData caseData = CaseData.builder()
                .id(12345L)
                .court(new Court("Name", "court@email.com", "001", "Region", null, null, null))
                .sendToCtsc("No")
                .build();

            AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(
                buildCaseDetails(caseData, migrationId));
            CaseData responseData = extractCaseData(response);

            assertThat(responseData.getSendToCtsc()).isEqualTo("Yes");
        }

    }
}
