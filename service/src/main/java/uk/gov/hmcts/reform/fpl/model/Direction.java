package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.OtherPartiesDirectionAssignee;
import uk.gov.hmcts.reform.fpl.enums.ParentsAndRespondentsDirectionAssignee;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.model.DirectionResponse;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Direction {
    @CCD(label = "Title", hint = "You must include a brief direction summary")
    private final String directionType;
    @CCD(label = "Description", hint = "Add more details", typeOverride = FieldType.TextArea)
    private String directionText;
    @CCD(label = "Status", showCondition = "directionNeeded!=\"No\"")
    private final String status;
    @CCD(
            label = "For",
            showCondition = "directionNeeded!=\"No\"",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DirectionAssignees",
            typeParameterClass = DirectionAssignees.class
    )
    private DirectionAssignee assignee;
    @CCD(
            label = "Assignee",
            showCondition = "directionNeeded!=\"No\"",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ParentsAndRespondentsDirectionAssignee"
    )
    private ParentsAndRespondentsDirectionAssignee parentsAndRespondentsAssignee;
    @CCD(
            label = "Assignee",
            showCondition = "directionNeeded!=\"No\"",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "OtherPartiesDirectionAssignee"
    )
    private OtherPartiesDirectionAssignee otherPartiesAssignee;
    @CCD(
            label = "Is this readOnly?",
            showCondition = "directionType CONTAINS \"DO NOT SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    private String readOnly;
    @CCD(
            label = "Is this direction needed?",
            showCondition = "directionType CONTAINS \"DO NOT SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    private String directionRemovable;
    @CCD(
            label = "Is this direction needed?",
            showCondition = "directionRemovable CONTAINS \"DO NOT SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    private String directionNeeded;
    @CCD(
            label = "Is this a custom direction?",
            showCondition = "directionType CONTAINS \"DO NOT SHOW\"",
            typeOverride = FieldType.YesOrNo
    )
    private String custom;
    @CCD(
            label = "Due date and time",
            hint = "For example, 31 3 2016 2 30 00",
            showCondition = "directionNeeded!=\"No\""
    )
    private LocalDateTime dateToBeCompletedBy;

    @JsonIgnore
    public boolean isNeeded() {
        return !"No".equals(this.directionNeeded);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "### For ###\nAll parties",
          showCondition = "directionNeeded=\"DO_NOT_SHOW\"",
          typeOverride = FieldType.Label
  )
  private String allParties_label;
  @CCD(label = " ", showCondition = "directionNeeded!=\"No\"")
  private DirectionResponse response;
  @CCD(label = "Compliance", showCondition = "directionNeeded!=\"No\"")
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<DirectionResponse>> responses;
  // ==== end synthesised definition-only fields ====
}
