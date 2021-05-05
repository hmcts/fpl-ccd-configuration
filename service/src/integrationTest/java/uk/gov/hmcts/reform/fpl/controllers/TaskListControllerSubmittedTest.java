package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
class TaskListControllerSubmittedTest extends AbstractCallbackTest {

    TaskListControllerSubmittedTest() {
        super("update-task-list");
    }

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @Test
    void shouldUpdateTaskList() {
        final CaseData caseData = CaseData.builder()
            .id(10L)
            .state(State.OPEN)
            .build();

        postSubmittedEvent(caseData);

        String expectedTaskList = readString("fixtures/taskList.md").trim();

        verify(coreCaseDataService).triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-task-list",
            Map.of("taskList", expectedTaskList));
    }
}
