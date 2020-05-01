package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Direction {
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
    private DirectionResponse response;
    private List<Element<DirectionResponse>> responses;

    public List<Element<DirectionResponse>> getResponses() {
        if (responses == null) {
            responses = new ArrayList<>();
        }
        return responses;
    }

    public Direction deepCopy() {
        List<Element<DirectionResponse>> responsesCopy = getResponses().stream()
            .map(responseElement -> Element.<DirectionResponse>builder()
                .id(responseElement.getId())
                .value(responseElement.getValue().toBuilder().build())
                .build())
            .collect(toList());

        DirectionResponse responseCopy = null;

        if (response != null) {
            responseCopy = response.toBuilder().build();
        }

        return this.toBuilder()
            .response(responseCopy)
            .responses(responsesCopy)
            .build();
    }

    @JsonIgnore
    public boolean isCompliedWith() {
        return this.response != null && this.response.getComplied() != null;
    }
}
