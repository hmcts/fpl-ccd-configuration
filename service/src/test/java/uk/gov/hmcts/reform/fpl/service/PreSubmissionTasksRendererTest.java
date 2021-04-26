package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.submission.PreSubmissionTask;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;

@ExtendWith(SpringExtension.class)
class PreSubmissionTasksRendererTest {
    private static final List<String> TEMPLATE_TEST = List.of(
        "<details class=\"govuk-details\" data-module=\"govuk-details\">",
        "<summary class=\"govuk-details__summary\">",
        "<span class=\"govuk-details__summary-text\" id=\"sp-msg-unselected-case-header\">",
        "Why can't I submit my application?",
        "</span>",
        "</summary>",
        "<div class=\"govuk-details__text\" id=\"sp-msg-unselected-case-content\">",
        "Add the orders and directions sought in the <a href=" +
            "'/case/PUBLICLAW/CARE_SUPERVISION_EPO/${[CASE_REFERENCE]}/trigger/ordersNeeded'>Orders and " +
            "directions sought</a>",
        "</div>",
        "</details>"
    );

    @InjectMocks
    PreSubmissionTasksRenderer underTest;

    @Test
    void shouldContainErrorMessageFromError() {
        final List<String> caseNameMessageLines = List.of("Add the orders and directions sought");
        List<PreSubmissionTask> validationErrors = List.of(
            PreSubmissionTask.builder().event(ORDERS_SOUGHT).messages(caseNameMessageLines).build()
        );

        assertThat(underTest.renderLines(validationErrors)).isEqualTo(TEMPLATE_TEST);
    }

    @Test
    void shouldRenderEmptyMessageWhenNoErrors() {
        assertThat(underTest.renderLines(List.of())).isEmpty();
    }
}
