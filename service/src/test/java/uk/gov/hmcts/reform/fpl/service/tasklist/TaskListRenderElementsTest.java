package uk.gov.hmcts.reform.fpl.service.tasklist;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TaskListRenderElementsTest {

    private static final String BASE_URL = "baseUrl";

    private TaskListRenderElements underTest = new TaskListRenderElements(BASE_URL);

    @Test
    void testRenderLink() {

        String actual = underTest.renderLink(Task.builder()
            .event(Event.ALLOCATION_PROPOSAL)
            .build());

        assertThat(actual).isEqualTo("<a href='/case/PUBLICLAW/CARE_SUPERVISION_EPO/"
            + "${[CASE_REFERENCE]}/trigger/otherProposal'>"
            + "Allocation proposal</a>");

    }
}
