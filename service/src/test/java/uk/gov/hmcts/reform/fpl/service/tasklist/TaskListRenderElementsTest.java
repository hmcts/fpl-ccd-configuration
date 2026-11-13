package uk.gov.hmcts.reform.fpl.service.tasklist;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;


class TaskListRenderElementsTest {

    private TaskListRenderElements underTest = new TaskListRenderElements();

    @Test
    void shouldRenderLink() {
        String actual = underTest.renderLink(Task.builder()
            .event(Event.ALLOCATION_PROPOSAL)
            .build());

        assertThat(actual).isEqualTo("<div class='govuk-grid-column-three-quarters'>"
            + "<a href='/case/PUBLICLAW/CARE_SUPERVISION_EPO/"
            + "${[CASE_REFERENCE]}/trigger/otherProposal'>"
            + "Allocation proposal</a></div>");
    }

    @Test
    void shouldRenderDisabledLink() {
        String actual = underTest.renderDisabledLink(Task.builder()
            .event(Event.ALLOCATION_PROPOSAL)
            .build());

        assertThat(actual).isEqualTo("<div class='govuk-grid-column-three-quarters'>"
            + "<a>Allocation proposal</a></div>");
    }

    @Test
    void shouldRenderHint() {
        String actual = underTest.renderHint("Test");

        assertThat(actual).isEqualTo("<span class='govuk-hint govuk-!-font-size-14'>Test</span>");
    }

    @Test
    void shouldRenderInfo() {
        String actual = underTest.renderInfo("Test");

        assertThat(actual).isEqualTo("<div class='panel panel-border-wide govuk-!-font-size-16'>Test</div>");
    }

    @Test
    void shouldRenderHeader() {
        String actual = underTest.renderHeader("Test");

        assertThat(actual).isEqualTo("## Test");
    }

    @Test
    void shouldRenderCollapsible() {
        List<String> actual = underTest.renderCollapsible("Test header", List.of("Line 1", "Line 2"));

        assertThat(actual).containsExactly(
            "<details class='govuk-details'>",
            "<summary class='govuk-details__summary'>",
            "<span class='govuk-details__summary-text'>",
            "Test header",
            "</span>",
            "</summary>",
            "<div class='govuk-details__text'>",
            "Line 1",
            "Line 2",
            "</div>",
            "</details>");
    }

    @Test
    void shouldRenderEmptyCollapsibleWhenNoContent() {
        List<String> actual = underTest.renderCollapsible("Test header", emptyList());

        assertThat(actual).containsExactly(
            "<details class='govuk-details'>",
            "<summary class='govuk-details__summary'>",
            "<span class='govuk-details__summary-text'>",
            "Test header",
            "</span>",
            "</summary>",
            "<div class='govuk-details__text'>",
            "</div>",
            "</details>");
    }
}
