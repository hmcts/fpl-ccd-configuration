package uk.gov.hmcts.reform.fpl.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidation;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Service
public class PreSubmissionTasksRenderer {

    //TODO: This should be replaced by a templating engine
    public String render(List<EventValidation> validationErrors) {
        if (!validationErrors.isEmpty()) {
            final List<String> lines = new LinkedList<>();

            lines.add("<details class=\"govuk-details\" data-module=\"govuk-details\"> ");
            lines.add("<summary class=\"govuk-details__summary\"> ");
            lines.add("<span class=\"govuk-details__summary-text\" id=\"sp-msg-unselected-case-header\">");
            lines.add("Why can't I submit my application?");
            lines.add("</span>");
            lines.add("</summary>");

            lines.add("<div class=\"govuk-details__text\" id=\"sp-msg-unselected-case-content\">");

            validationErrors.forEach(validationError -> {
                lines.addAll(renderEventValidation(validationError));
            });

            lines.add("</div>");

            lines.add("</details>");
            return String.join("\n\n", lines);
        }

        return "";
    }

    private static String renderLink(Event event) {
        return format("<a href='/case/%s/%s/${[CASE_REFERENCE]}/trigger/%s'>%s</a>",
            JURISDICTION, CASE_TYPE, event.getId(), event.getName());
    }

    private static List<String> renderEventValidation(EventValidation eventValidation) {
        return eventValidation.getMessages().stream().map(
            message -> String.format("%s in the %s", message, renderLink(eventValidation.getEvent()))
        ).collect(Collectors.toList());
    }
}
