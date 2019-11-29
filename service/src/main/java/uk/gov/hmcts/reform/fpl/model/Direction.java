package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Direction {
    private final String directionType;
    private String directionText;
    private final String status;
    private DirectionAssignee assignee;
    private String readOnly;
    private String directionRemovable;
    private String directionNeeded;
    private String custom;
    private LocalDateTime dateToBeCompletedBy;
    private DirectionResponse response;
    private List<Element<DirectionResponse>> responses;

    public List<Element<DirectionResponse>> getResponses() {
        if (responses == null) {
            responses = new ArrayList<>();
        }
        return responses;
    }
}
