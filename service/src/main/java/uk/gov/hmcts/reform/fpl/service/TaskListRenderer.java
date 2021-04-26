package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.submission.PreSubmissionTask;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskSection;
import uk.gov.hmcts.reform.fpl.service.tasklist.TaskListRenderElements;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.List.of;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.COURT_SERVICES;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORGANISATION_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskSection.newSection;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListRenderer {

    private static final String HORIZONTAL_LINE = "<hr class='govuk-!-margin-top-3 govuk-!-margin-bottom-2'/>";
    private static final String NEW_LINE = "<br/>";

    private final TaskListRenderElements taskListRenderElements;

    private final PreSubmissionTasksRenderer preSubmissionTasksRenderer;

    //TODO consider templating solution like mustache
    public String render(List<Task> allTasks, List<PreSubmissionTask> preSubmissionTasks) {
        final List<String> lines = new LinkedList<>();

        lines.add("<div class='width-50'>");

        groupInSections(allTasks).forEach(section -> lines.addAll(renderSection(section)));

        lines.add("</div>");

        lines.addAll(preSubmissionTasksRenderer.renderLines(preSubmissionTasks));

        return String.join("\n\n", lines);
    }

    private List<TaskSection> groupInSections(List<Task> allTasks) {
        final Map<Event, Task> tasks = allTasks.stream().collect(toMap(Task::getEvent, identity()));

        final TaskSection applicationDetails = newSection("Add application details", of(
            tasks.get(CASE_NAME),
            tasks.get(ORDERS_SOUGHT),
            tasks.get(HEARING_URGENCY)
        ));

        final TaskSection applicationGrounds = newSection("Add grounds for the application", of(
            tasks.get(GROUNDS),
            tasks.get(RISK_AND_HARM)
                .withHint("In emergency cases, you can send your application without this information"),
            tasks.get(FACTORS_AFFECTING_PARENTING)
                .withHint("In emergency cases, you can send your application without this information")
        ));

        final TaskSection documents = newSection("Add application documents",
            of(tasks.get(APPLICATION_DOCUMENTS)))
            .withHint("For example, SWET, social work chronology and care plan<br> In emergency cases, "
                + "you can send your application without this information ");

        final TaskSection parties = newSection("Add information about the parties",
            List.of(
                tasks.get(ORGANISATION_DETAILS),
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
            tasks.get(COURT_SERVICES)
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

    private List<String> renderSection(TaskSection sec) {
        final List<String> section = new LinkedList<>();

        section.add(NEW_LINE);
        section.add(taskListRenderElements.renderHeader(sec.getName()));

        sec.getHint().map(taskListRenderElements::renderHint).ifPresent(section::add);
        sec.getInfo().map(taskListRenderElements::renderInfo).ifPresent(section::add);

        section.add(HORIZONTAL_LINE);
        sec.getTasks().forEach(task -> {
            section.addAll(renderTask(task));
            section.add(HORIZONTAL_LINE);
        });

        return section;
    }

    private List<String> renderTask(Task task) {
        final List<String> lines = new LinkedList<>();

        switch (task.getState()) {
            case NOT_AVAILABLE:
                lines.add(taskListRenderElements.renderDisabledLink(task)
                    + taskListRenderElements.renderImage("cannot-send-yet.png", "Cannot send yet"));
                break;
            case IN_PROGRESS:
                lines.add(taskListRenderElements.renderLink(task)
                    + taskListRenderElements.renderImage("in-progress.png", "In progress"));
                break;
            case COMPLETED:
                lines.add(taskListRenderElements.renderLink(task)
                    + taskListRenderElements.renderImage("information-added.png", "Information added"));
                break;
            case COMPLETED_FINISHED:
                lines.add(taskListRenderElements.renderLink(task)
                    + taskListRenderElements.renderImage("finished.png", "Finished"));
                break;
            default:
                lines.add(taskListRenderElements.renderLink(task));
        }

        task.getHint().map(taskListRenderElements::renderHint).ifPresent(lines::add);
        return lines;
    }


}
