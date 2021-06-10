package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardDirectionTemplate {
    private final String directionType;
    private String directionText;
    private final String status;
    private DirectionAssignee assignee;
    private ParentsAndRespondentsDirectionAssignee parentsAndRespondentsAssignee;
    private OtherPartiesDirectionAssignee otherPartiesAssignee;
    private String readOnly;
    private String directionRemovable;
    private String directionNeeded;
    private String custom;
    private LocalDateTime dateToBeCompletedBy;

    @JsonIgnore
    public boolean isNeeded() {
        return !"No".equals(this.directionNeeded);
    }
}
