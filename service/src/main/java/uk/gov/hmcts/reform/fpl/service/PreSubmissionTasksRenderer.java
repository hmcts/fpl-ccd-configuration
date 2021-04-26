package uk.gov.hmcts.reform.fpl.service;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.submission.PreSubmissionTask;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Service
public class PreSubmissionTasksRenderer {
    private static final List<String> PRE_TEMPLATE = List.of(
        "<details class=\"govuk-details\" data-module=\"govuk-details\">",
        "<summary class=\"govuk-details__summary\">",
        "<span class=\"govuk-details__summary-text\" id=\"sp-msg-unselected-case-header\">",
        "Why can't I submit my application?",
        "</span>",
        "</summary>",
        "<div class=\"govuk-details__text\" id=\"sp-msg-unselected-case-content\">"
    );

    private static final List<String> POST_TEMPLATE = List.of(
        "</div>",
        "</details>"
    );

    //TODO: This should be replaced by a templating engine
    public List<String> renderLines(List<PreSubmissionTask> preSubmissionTasks) {
        if (!preSubmissionTasks.isEmpty()) {
            final List<String> lines = new ArrayList<>();

            lines.addAll(PRE_TEMPLATE);

            lines.addAll(preSubmissionTasks.stream().flatMap(
                task -> renderEventValidation(task).stream()
            ).filter(ObjectUtils::isNotEmpty).collect(toList()));

            lines.addAll(POST_TEMPLATE);

            return lines;
        }
        return List.of();
    }

    private String renderLink(Event event) {
        return format("<a href='/case/%s/%s/${[CASE_REFERENCE]}/trigger/%s'>%s</a>",
            JURISDICTION, CASE_TYPE, event.getId(), event.getName());
    }

    private List<String> renderEventValidation(PreSubmissionTask preSubmissionTask) {
        return preSubmissionTask.getMessages().stream().map(
            message -> String.format("%s in the %s", message, renderLink(preSubmissionTask.getEvent()))
        ).collect(toList());
    }
}
