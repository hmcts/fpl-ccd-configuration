package uk.gov.hmcts.reform.fpl.model.workallocation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.Map;

@Value
public class UserTask {

    @JsonProperty("complete_task")
    boolean completeTask;

    @JsonProperty("task_data")
    Map<String, Object> taskData;
}
