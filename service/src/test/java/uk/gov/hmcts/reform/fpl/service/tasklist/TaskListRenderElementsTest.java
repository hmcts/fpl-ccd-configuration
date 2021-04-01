package uk.gov.hmcts.reform.fpl.service.tasklist;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TaskListRenderElementsTest {

    private static final String BASE_URL = "baseUrl/";

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

    @Test
    void testRenderDisabledLink() {
        String actual = underTest.renderDisabledLink(Task.builder()
            .event(Event.ALLOCATION_PROPOSAL)
            .build());

        assertThat(actual).isEqualTo("<a>Allocation proposal</a>");
    }

    @Test
    void testRenderImage() {
        String actual = underTest.renderImage("imageName", "title");

        assertThat(actual).isEqualTo("<img align='right' height='25px' src='baseUrl/imageName' title='title'/>");
    }

    @Test
    void testRenderHint() {
        String actual = underTest.renderHint("Test");

        assertThat(actual).isEqualTo("<span class='govuk-hint govuk-!-font-size-14'>Test</span>");
    }

    @Test
    void testRenderInfo() {
        String actual = underTest.renderInfo("Test");

        assertThat(actual).isEqualTo("<div class='panel panel-border-wide govuk-!-font-size-16'>Test</div>");
    }

    @Test
    void testRenderHeader() {
        String actual = underTest.renderHeader("Test");

        assertThat(actual).isEqualTo("## Test");
    }
}
