package uk.gov.hmcts.reform.fpl.model.workallocation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class ClientContext {

    @JsonProperty("user_task")
    UserTask userTask;
}
