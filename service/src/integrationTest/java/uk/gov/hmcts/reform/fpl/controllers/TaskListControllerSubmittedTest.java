package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
class TaskListControllerSubmittedTest extends AbstractCallbackTest {

    TaskListControllerSubmittedTest() {
        super("update-task-list");
    }

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private FeatureToggleService featureToggleService;

    final CaseData caseData = CaseData.builder()
        .id(10L)
        .state(State.OPEN)
        .build();

    @Test
    void shouldUpdateTaskListWithAdditionalContactsToggledOff() {
        final CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .id(10L)
            .state(State.OPEN)
            .build();

        when(featureToggleService.isApplicantAdditionalContactsEnabled()).thenReturn(false);

        postSubmittedEvent(caseData);

        String expectedTaskList = readString("fixtures/taskList-legacyApplicant.md").trim();

        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-task-list",
            Map.of("taskList", expectedTaskList));
    }

    @Test
    void shouldUpdateTaskListWithAdditionalContactsToggledOn() {
        when(featureToggleService.isApplicantAdditionalContactsEnabled()).thenReturn(true);
        when(featureToggleService.isLanguageRequirementsEnabled()).thenReturn(false);

        postSubmittedEvent(caseData);

        String expectedTaskList = readString("fixtures/taskList.md").trim();

        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-task-list",
            Map.of("taskList", expectedTaskList));
    }

    @Test
    void shouldIncludeLanguageSelectionIfToggledOn() {
        when(featureToggleService.isLanguageRequirementsEnabled()).thenReturn(true);

        postSubmittedEvent(caseData);

        String expectedTaskList = readString("fixtures/taskListWithLanguageRequirement.md").trim();

        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-task-list",
            Map.of("taskList", expectedTaskList));
    }
}
