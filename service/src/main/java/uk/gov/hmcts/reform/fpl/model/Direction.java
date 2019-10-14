package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Direction {
    private final UUID id;
    private final String directionType;
    private String directionText;
    private final String status;
    private final DirectionAssignee assignee;
    private String readOnly;
    private String directionRemovable;
    private String directionNeeded;
    private String custom;
    private LocalDateTime dateToBeCompletedBy;
}
