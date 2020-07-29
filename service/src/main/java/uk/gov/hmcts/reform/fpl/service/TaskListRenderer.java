package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskSection;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.List.of;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_NEEDED;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_NEEDED;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskSection.newSection;

@Service
public class TaskListRenderer {

    private final String imagesBaseUrl;

    public TaskListRenderer(@Value("${resources.images.baseUrl}") String imagesBaseUrl) {
        this.imagesBaseUrl = imagesBaseUrl;
    }

    private List<TaskSection> groupInSections(List<Task> allTasks) {
        final Map<Event, Task> tasks = allTasks.stream().collect(toMap(Task::getEvent, identity()));

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
                .withHint("For example, SWET, social work chronology and care plan");

        final TaskSection parties = newSection("Add information about the parties",
                List.of(
                        tasks.get(APPLICANT),
                        tasks.get(CHILDREN),
                        tasks.get(RESPONDENTS)
                ));

        final TaskSection courtRequirements = newSection("Add court requirements", of(
                tasks.get(ALLOCATION_PROPOSAL)
        ));

        final TaskSection additionalInformation = newSection("Add additional information", of(
                tasks.get(OTHER_PROCEEDINGS),
                tasks.get(INTERNATIONAL_ELEMENT),
                tasks.get(OTHERS),
                tasks.get(ATTENDING_THE_HEARING)
        )).withInfo("Only complete if relevant");

        final TaskSection sentApplication = newSection("Send application", of(tasks.get(SUBMIT_APPLICATION)));

        return List.of(applicationDetails,
                applicationGrounds,
                documents,
                parties,
                courtRequirements,
                additionalInformation,
                sentApplication);
    }

    //TODO consider templating solution like mustache
    public String render(List<Task> allTasks) {
        final List<String> lines = new LinkedList<>();

        lines.add("<div class='width-50'>");

        groupInSections(allTasks)
                .forEach(section -> lines.addAll(renderSection(section)));

        lines.add("</div>");

        return String.join("\n\n", lines);
    }

    private List<String> renderSection(TaskSection sec) {
        final List<String> section = new LinkedList<>();

        section.add(renderNewLine());
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
        final List<String> lines = new LinkedList<>();

        switch (task.getState()) {
            case NOT_AVAILABLE:
                lines.add(renderDisabledLink(task) + renderImage("cannot-send-yet.png", "Cannot send yet"));
                break;
            case COMPLETED:
                lines.add(renderLink(task) + renderImage("information-added.png", "Information added"));
                break;
            default:
                lines.add(renderLink(task));
        }

        task.getHint().map(this::renderHint).ifPresent(lines::add);
        return lines;
    }

    private String renderLink(Task event) {
        return format("<a href='/case/%s/%s/${[CASE_REFERENCE]}/trigger/%s'>%s</a>",
                JURISDICTION, CASE_TYPE, event.getEvent().getId(), event.getEvent().getName());
    }

    private String renderDisabledLink(Task event) {
        return format("<a>%s</a>", event.getEvent().getName());
    }

    private String renderImage(String imageName, String title) {
        return format("<img align='right' height='25px' src='%s%s' title='%s'/>", imagesBaseUrl, imageName, title);
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

    private String renderNewLine() {
        return "<br/>";
    }
}
