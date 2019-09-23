package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Direction {
    private final String type;
    private final String assignee;
    private final String status;
    private  String text;
    private String readOnly;
    //That's need proper datetime type  and parsing
    private String completeBy;

}
