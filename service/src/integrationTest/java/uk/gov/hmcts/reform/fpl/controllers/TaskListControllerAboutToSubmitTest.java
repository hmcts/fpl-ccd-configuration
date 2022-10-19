package uk.gov.hmcts.reform.fpl.controllers;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Map;

@WebMvcTest(AddCaseNumberController.class)
@OverrideAutoConfiguration(enabled = true)
class TaskListControllerAboutToSubmitTest extends AbstractCallbackTest {

    TaskListControllerAboutToSubmitTest() {
        super("update-task-list");
    }

    @Test
    void shouldPopulateUpdatedCaseNameToGlobalSearchCaseNames() {
        Map<String, Object> caseDetails = postAboutToSubmitEvent(CaseData.builder()
            .id(10L)
            .state(State.OPEN)
            .caseName("Updated CaseName")
            .build()).getData();

        Assertions.assertThat(caseDetails.get("caseNameHmctsInternal")).isEqualTo("Updated CaseName");

    }
}
