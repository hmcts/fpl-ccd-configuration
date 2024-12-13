package uk.gov.hmcts.reform.fpl.model.tasklist;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Getter
public class TaskSection {

    private final String name;
    private final String welshName;
    private List<Task> tasks;
    private String hint;
    private String hintWelsh;
    private String info;
    private String infoWelsh;

    private TaskSection(String name, String welshName, List<Task> tasks) {
        this.name = name;
        this.welshName = welshName;
        this.tasks = tasks;
    }

    public static TaskSection newSection(String name) {
        return newSection(name, null);
    }

    public static TaskSection newSection(String name, String welshName) {
        return new TaskSection(name, welshName, new ArrayList<>());
    }

    public TaskSection withTask(Task task) {
        ofNullable(task).ifPresent(tasks::add);
        return this;
    }

    public TaskSection withHint(String hint) {
        this.hint = hint;
        return this;
    }

    public TaskSection withHint(String hint, String hintWelsh) {
        this.hint = hint;
        this.hintWelsh = hintWelsh;
        return this;
    }

    public TaskSection withInfo(String info) {
        this.info = info;
        return this;
    }

    public TaskSection withInfo(String info, String infoWelsh) {
        this.info = info;
        this.infoWelsh = infoWelsh;
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

    public String getNameInLang(boolean welsh) {
        return welsh ? welshName : name;
    }

    public Optional<String> getHintInLang(boolean welsh) {
        return Optional.ofNullable(welsh ? hintWelsh : hint);
    }

    public Optional<String> getInfoInLang(boolean welsh) {
        return Optional.ofNullable(welsh ? infoWelsh : info);
    }

}
