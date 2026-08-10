package uk.gov.hmcts.reform.fpl.model.judicialmessage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.json.deserializer.DynamicListDeserializer;
import uk.gov.hmcts.reform.fpl.json.serializer.YesNoSerializer;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageSenderRoleTypes;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageRoleTypes;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JudicialMessageMetaData {
    @CCD(
            label = "Sender",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "JudicialMessageSenderRoleTypes",
            typeParameterClass = JudicialMessageSenderRoleTypes.class
    )
    private final JudicialMessageRoleType senderType;
    @CCD(label = "Sender's email address", typeOverride = FieldType.Email)
    private final String sender;
    @CCD(
            label = "Recipient",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "JudicialMessageRoleTypes",
            typeParameterClass = JudicialMessageRoleTypes.class
    )
    private final JudicialMessageRoleType recipientType;
    @CCD(label = "Recipient", typeOverride = FieldType.DynamicList)
    @JsonDeserialize(using = DynamicListDeserializer.class)
    private final DynamicList recipientDynamicList;
    @CCD(label = "Message history", typeOverride = FieldType.TextArea)
    private final String messageHistoryTemp;
    @CCD(label = "Recipient's email address", typeOverride = FieldType.Email)
    private final String recipient;
    @CCD(label = "Message from")
    private final String recipientLabel;
    @CCD(label = "Message subject")
    @JsonProperty("requestedBy")
    private final String subject;
    @CCD(label = "Urgency", hint = "Add if it's urgent, or if a response is requested within a specified time.")
    private final String urgency;
    @CCD(label = "Urgency", typeOverride = FieldType.YesOrNo)
    @JsonSerialize(using = YesNoSerializer.class)
    private final YesNo isJudicialMessageUrgent;
    @CCD(label = "Application")
    private final String applicationType;

    // For display on the tab
    @CCD(label = "From")
    private final String fromLabel;
    @CCD(label = "To")
    private final String toLabel;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<div class='govuk-warning-text'><span class='govuk-warning-text__icon' aria-hidden='true'>!</span><strong class='govuk-warning-text__text'>Using this option will not auto-assign a task as they are not the hearing/allocated judge. After the task has been created, you must assign it manually on the Tasks tab to ensure it isn't missed.</strong></div>",
          showCondition = "otherJudgeWarningLabel=\"DO_NOT_SHOW\"",
          typeOverride = FieldType.Label
  )
  private String otherJudgeWarningLabel;
  // ==== end synthesised definition-only fields ====
}
