package uk.gov.hmcts.reform.fpl.model.tasklist;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Getter
public class TaskSection {

    private final String name;
    private List<Task> tasks;
    private String hint;
    private String info;

    private TaskSection(String name, List<Task> tasks) {
        this.name = name;
        this.tasks = tasks;
    }

    public static TaskSection newSection(String name) {
        return new TaskSection(name, new ArrayList<>());
    }

    public TaskSection withTask(Task task) {
        tasks.add(task);
        return this;
    }

    public TaskSection withHint(String hint) {
        this.hint = hint;
        return this;
    }

    public TaskSection withInfo(String info) {
        this.info = info;
        return this;
    }

    public Optional<String> getHint() {
        return Optional.ofNullable(hint);
    }

    public Optional<String> getInfo() {
        return Optional.ofNullable(info);
    }

    public boolean hasAnyTask() {
        return isNotEmpty(tasks);
    }
}
