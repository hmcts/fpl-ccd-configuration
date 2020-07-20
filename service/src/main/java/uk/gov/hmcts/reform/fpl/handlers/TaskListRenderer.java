package uk.gov.hmcts.reform.fpl.handlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.EventState;
import uk.gov.hmcts.reform.fpl.FplEvent;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.EventState.COMPLETED;
import static uk.gov.hmcts.reform.fpl.EventState.NOT_AVAILABLE;
import static uk.gov.hmcts.reform.fpl.FplEvent.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.FplEvent.APPLICANT;
import static uk.gov.hmcts.reform.fpl.FplEvent.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.fpl.FplEvent.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.FplEvent.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.FplEvent.ENTER_CHILDREN;
import static uk.gov.hmcts.reform.fpl.FplEvent.ENTER_OTHERS;
import static uk.gov.hmcts.reform.fpl.FplEvent.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.FplEvent.GROUNDS;
import static uk.gov.hmcts.reform.fpl.FplEvent.HEARING_NEEDED;
import static uk.gov.hmcts.reform.fpl.FplEvent.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.FplEvent.ORDERS_NEEDED;
import static uk.gov.hmcts.reform.fpl.FplEvent.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.FplEvent.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.FplEvent.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.FplEvent.SUBMIT_APPLICATION;

@Service
public class TaskListRenderer {

    private final String imagesBaseUrl;

    public TaskListRenderer(@Value("${resources.images.baseUrl}") String imagesBaseUrl) {
        this.imagesBaseUrl = imagesBaseUrl;
    }

    public String render(Map<FplEvent, EventState> events) {
        final List<String> messages = new LinkedList<>();

        messages.add("<div class='width-50'>");

        messages.addAll(section(
            "Add application details",
            selectEvents(events, CASE_NAME, ORDERS_NEEDED, HEARING_NEEDED)));

        messages.addAll(section(
            "Add grounds for the application",
            selectEvents(events, GROUNDS, RISK_AND_HARM, FACTORS_AFFECTING_PARENTING)));

        messages.addAll(section(
            "Add supporting documents",
            selectEvents(events, DOCUMENTS),
            "For example, the social work chronology and care plan",
            null));

        messages.addAll(section(
            "Add information about the parties",
            selectEvents(events, APPLICANT, ENTER_CHILDREN, RESPONDENTS)));

        messages.addAll(section(
            "Add court requirements",
            selectEvents(events, ALLOCATION_PROPOSAL)));

        messages.addAll(section(
            "Add additional information",
            selectEvents(events, OTHER_PROCEEDINGS, INTERNATIONAL_ELEMENT, ENTER_OTHERS, ATTENDING_THE_HEARING),
            null,
            "Only complete if relevant"));

        messages.addAll(section(
            "Submit application",
            selectEvents(events, SUBMIT_APPLICATION)));

        messages.add("</div>");

        return String.join("\n\n", messages);
    }

    private Map<FplEvent, EventState> selectEvents(Map<FplEvent, EventState> allEvents, FplEvent... events) {
        final List<FplEvent> requiredEvents = Arrays.asList(events);

        return allEvents.entrySet().stream()
            .filter(event -> requiredEvents.contains(event.getKey()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<String> section(String name, Map<FplEvent, EventState> events) {
        return section(name, events, null, null);
    }

    private List<String> section(String name, Map<FplEvent, EventState> events, String hint, String info) {
        final List<String> section = new LinkedList<>();

        section.add(header(name));

        ofNullable(hint).map(this::hint).ifPresent(section::add);
        ofNullable(info).map(this::info).ifPresent(section::add);

        section.add(horizontalLine());
        events.forEach((event, status) -> {
            section.add(event(event, status));
            section.add(horizontalLine());
        });

        return section;
    }

    private String event(FplEvent event, EventState state) {
        if (state == NOT_AVAILABLE) {
            return event.getName() + image("cannot-start-yet.png");
        }

        if (state == COMPLETED) {
            return link(event) + image("information-added.png");
        }

        return link(event);
    }

    private String link(FplEvent event) {
        return format("[%s](/case/%s/%s/${[CASE_REFERENCE]}/trigger/%s)", event.getName(), JURISDICTION, CASE_TYPE, event.getId());
    }

    private String image(String image) {
        return format("<img align='right' height='25px' src='%s%s'>", imagesBaseUrl, image);
    }

    private String hint(String text) {
        return format("<span class='govuk-hint govuk-!-font-size-16'>%s</span>", text);
    }

    private String info(String text) {
        return format("<div class='panel panel-border-wide govuk-!-font-size-16'>%s</div>", text);
    }

    private String header(String text) {
        return format("## %s", text);
    }

    private String horizontalLine() {
        return "<hr class='govuk-!-margin-top-3 govuk-!-margin-bottom-2'/>";
    }
}
