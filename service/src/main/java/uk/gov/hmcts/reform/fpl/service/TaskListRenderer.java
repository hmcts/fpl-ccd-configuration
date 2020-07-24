package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.Task;
import uk.gov.hmcts.reform.fpl.TaskSection;
import uk.gov.hmcts.reform.fpl.enums.Event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.List.of;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.TaskSection.newSection;
import static uk.gov.hmcts.reform.fpl.TaskState.COMPLETED;
import static uk.gov.hmcts.reform.fpl.TaskState.NOT_AVAILABLE;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ENTER_CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.ENTER_OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_NEEDED;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_NEEDED;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;

@Service
public class TaskListRenderer {

    private final String imagesBaseUrl;

    public TaskListRenderer(@Value("${resources.images.baseUrl}") String imagesBaseUrl) {
        this.imagesBaseUrl = imagesBaseUrl;
    }

    public String render(List<Task> allTasks) {
        final List<String> lines = new LinkedList<>();

        final Map<Event, Task> tasks = allTasks.stream().collect(toMap(event -> event.getEvent(), identity()));

        final TaskSection applicationDetails = newSection("Add application details", of(
            tasks.get(CASE_NAME),
            tasks.get(ORDERS_NEEDED),
            tasks.get(HEARING_NEEDED)
        ));

        final TaskSection applicationGrounds = newSection("Add grounds for the application", of(
            tasks.get(GROUNDS),
            tasks.get(RISK_AND_HARM)
                .withHint("In emergency cases, you can send your application without this information"),
            tasks.get(FACTORS_AFFECTING_PARENTING)
                .withHint("In emergency cases, you can send your application without this information")
        ));

        final TaskSection documents = newSection("Add supporting documents", of(tasks.get(DOCUMENTS)))
            .withHint("For example, the social work chronology and care plan");

        final TaskSection parties = newSection("Add information about the parties",
            List.of(
                tasks.get(APPLICANT),
                tasks.get(ENTER_CHILDREN),
                tasks.get(RESPONDENTS)
            ));

        final TaskSection courtRequirements = newSection("Add court requirements", of(
            tasks.get(ALLOCATION_PROPOSAL)
        ));

        final TaskSection additionalInformation = newSection("Add additional information", of(
            tasks.get(OTHER_PROCEEDINGS),
            tasks.get(INTERNATIONAL_ELEMENT),
            tasks.get(ENTER_OTHERS),
            tasks.get(ATTENDING_THE_HEARING)
        )).withInfo("Only complete if relevant");

        final TaskSection sentApplication = newSection("Send application", of(tasks.get(SUBMIT_APPLICATION)));

        lines.add("<div class='width-50'>");

        Stream.of(
            applicationDetails,
            applicationGrounds,
            documents,
            parties,
            courtRequirements,
            additionalInformation,
            sentApplication).forEach(section -> lines.addAll(renderSection(section)));

        lines.add("</div>");

        return String.join("\n\n", lines);
    }

    private List<String> renderSection(TaskSection sec) {
        final List<String> section = new LinkedList<>();

        section.add("<br/>");
        section.add(renderHeader(sec.getName()));

        sec.getHint().map(this::renderHint).ifPresent(section::add);
        sec.getInfo().map(this::renderInfo).ifPresent(section::add);

        section.add(renderHorizontalLine());
        sec.getTasks().forEach(task -> {
            section.addAll(renderTask(task));
            section.add(renderHorizontalLine());
        });

        return section;
    }

    private List<String> renderTask(Task task) {
        final List<String> lines = new ArrayList<>();
        if (task.getState() == NOT_AVAILABLE) {
            lines.add(task.getEvent().getName() + renderImage("cannot-send-yet.png"));
        } else if (task.getState() == COMPLETED) {
            lines.add(renderLink(task) + renderImage("information-added.png"));
        } else {
            lines.add(renderLink(task));
        }

        task.getHint().map(this::renderHint).ifPresent(lines::add);

        return lines;
    }

    private String renderLink(Task event) {
        return format("[%s](/case/%s/%s/${[CASE_REFERENCE]}/trigger/%s)", event.getEvent().getName(),
            JURISDICTION, CASE_TYPE, event.getEvent().getId());
    }

    private String renderImage(String image) {
        return format("<img align='right' height='25px' src='%s%s'>", imagesBaseUrl, image);
    }

    private String renderHint(String text) {
        return format("<span class='govuk-hint govuk-!-font-size-14'>%s</span>", text);
    }

    private String renderInfo(String text) {
        return format("<div class='panel panel-border-wide govuk-!-font-size-16'>%s</div>", text);
    }

    private String renderHeader(String text) {
        return format("## %s", text);
    }

    private String renderHorizontalLine() {
        return "<hr class='govuk-!-margin-top-3 govuk-!-margin-bottom-2'/>";
    }
}
