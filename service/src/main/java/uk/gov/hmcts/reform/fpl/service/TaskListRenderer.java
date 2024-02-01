package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidationErrors;
import uk.gov.hmcts.reform.fpl.model.tasklist.Task;
import uk.gov.hmcts.reform.fpl.model.tasklist.TaskSection;
import uk.gov.hmcts.reform.fpl.service.tasklist.TaskListRenderElements;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.COURT_SERVICES;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.LOCAL_AUTHORITY_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORGANISATION_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.enums.Event.SELECT_COURT;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;
import static uk.gov.hmcts.reform.fpl.model.tasklist.TaskSection.newSection;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListRenderer {

    private static final String HORIZONTAL_LINE = "<hr class='govuk-!-margin-top-3 govuk-!-margin-bottom-2'/>";
    private static final String NEW_LINE = "<br/>";

    private final TaskListRenderElements taskListRenderElements;
    private final FeatureToggleService featureToggleService;

    public String render(List<Task> allTasks, List<EventValidationErrors> taskErrors) {
        return render(allTasks, taskErrors, Optional.empty(), Optional.empty());
    }

    //TODO consider templating solution like mustache
    public String render(List<Task> allTasks, List<EventValidationErrors> tasksErrors,
                         Optional<String> applicationType, Optional<Map<Event, String>> tasksHints) {
        final List<String> lines = new LinkedList<>();

        lines.add("<div class='width-50'>");
        applicationType.ifPresent(s -> {
            lines.add(NEW_LINE);
            lines.add(String.format("<div class='govuk-tag govuk-tag--blue'>%s Application</div>", s));
        });

        groupInSections(allTasks, tasksHints).forEach(section -> lines.addAll(renderSection(section)));

        lines.add("</div>");

        lines.addAll(renderTasksErrors(tasksErrors));

        return String.join("\n\n", lines);
    }

    private List<TaskSection> groupInSections(List<Task> allTasks, Optional<Map<Event, String>> tasksHints) {
        final Map<Event, Task> tasks = allTasks.stream().collect(toMap(Task::getEvent, identity()));

        tasksHints.ifPresent(tasksHintsMap -> tasksHintsMap.forEach((event, hint) -> tasks.get(event).withHint(hint)));

        final TaskSection applicationDetails = newSection("Add application details")
            .withTask(tasks.get(CASE_NAME))
            .withTask(tasks.get(ORDERS_SOUGHT))
            .withTask(tasks.get(HEARING_URGENCY));

        final TaskSection applicationGrounds = newSection("Add grounds for the application");

        ofNullable(tasks.get(GROUNDS))
            .ifPresent(applicationGrounds::withTask);
        ofNullable(tasks.get(RISK_AND_HARM))
            .map(task -> task.withHint("In emergency cases, you can send your application without this information"))
            .ifPresent(applicationGrounds::withTask);
        ofNullable(tasks.get(FACTORS_AFFECTING_PARENTING))
            .map(task -> task.withHint("In emergency cases, you can send your application without this information"))
            .ifPresent(applicationGrounds::withTask);

        final TaskSection documents = newSection("Add application documents")
            .withTask(tasks.get(APPLICATION_DOCUMENTS))
            .withHint("For example, SWET, social work chronology and care plan<br> In emergency cases, "
                + "you can send your application without this information ");

        final TaskSection parties = newSection("Add information about the parties")
            .withTask(tasks.containsKey(ORGANISATION_DETAILS)
                ? tasks.get(ORGANISATION_DETAILS) : tasks.get(LOCAL_AUTHORITY_DETAILS))
            .withTask(tasks.get(CHILDREN))
            .withTask(tasks.get(RESPONDENTS));

        final TaskSection courtRequirements = newSection("Add court requirements")
            .withTask(tasks.get(ALLOCATION_PROPOSAL));
        ofNullable(tasks.get(SELECT_COURT)).ifPresent(courtRequirements::withTask);

        final TaskSection additionalInformation = newSection("Add additional information")
            .withTask(tasks.get(C1_WITH_SUPPLEMENT))
            .withTask(tasks.get(OTHER_PROCEEDINGS))
            .withTask(tasks.get(INTERNATIONAL_ELEMENT))
            .withTask(tasks.get(OTHERS))
            .withTask(tasks.get(COURT_SERVICES))
            .withInfo("Only complete if relevant");

        if (featureToggleService.isLanguageRequirementsEnabled()) {
            additionalInformation.withTask(tasks.get(LANGUAGE_REQUIREMENTS));
        }

        final TaskSection sentApplication = newSection("Send application")
            .withTask(tasks.get(SUBMIT_APPLICATION));

        return Stream.of(applicationDetails,
            applicationGrounds,
            documents,
            parties,
            courtRequirements,
            additionalInformation,
            sentApplication)
            .filter(TaskSection::hasAnyTask)
            .collect(toList());
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

    private List<String> renderTasksErrors(List<EventValidationErrors> taskErrors) {
        if (isEmpty(taskErrors)) {
            return emptyList();
        }
        final List<String> errors = taskErrors.stream()
            .flatMap(task -> task.getErrors()
                .stream()
                .map(error -> format("%s in the %s", error, taskListRenderElements.renderLink(task.getEvent()))))
            .collect(toList());

        return taskListRenderElements.renderCollapsible("Why can't I submit my application?", errors);
    }

}
