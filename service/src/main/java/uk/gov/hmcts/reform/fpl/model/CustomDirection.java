package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class CustomDirection {
    private String id; //remove?
    private String title;
    private String description;
    private DirectionAssignee assignee;
    private LocalDateTime dateToBeCompletedBy;
}
