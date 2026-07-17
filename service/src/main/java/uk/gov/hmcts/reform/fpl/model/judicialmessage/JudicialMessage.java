package uk.gov.hmcts.reform.fpl.model.judicialmessage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@ToString(callSuper = true)
public class JudicialMessage extends JudicialMessageMetaData {
    private static final int MAX_DYNAMIC_LIST_LABEL_LENGTH = 250;

    @CCD(label = "Date sent")
    private final String dateSent;
    @CCD(label = " ", showCondition = "dateSent=\"DO_NOT_SHOW\"")
    private final LocalDateTime updatedTime;
    @CCD(label = "Status")
    private final JudicialMessageStatus status;
    @CCD(label = "Related documents", typeOverride = FieldType.Collection, typeParameterOverride = "Document")
    private final List<Element<DocumentReference>> relatedDocuments;
    @CCD(
            label = " ",
            showCondition = "relatedDocuments=\"DO_NOT_SHOW\"",
            retainHiddenValue = true,
            typeOverride = FieldType.TextArea
    )
    private final String relatedDocumentFileNames;
    @CCD(
            label = " ",
            showCondition = "relatedDocuments=\"DO_NOT_SHOW\"",
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    private final YesNo isRelatedToC2;
    @CCD(label = " ", typeOverride = FieldType.YesOrNo)
    private final String isReplying;
    @CCD(label = "Latest message", showCondition = "status!=\"CLOSED\"", typeOverride = FieldType.TextArea)
    private final String latestMessage;
    @CCD(label = "Message history (old)", typeOverride = FieldType.TextArea)
    private final String messageHistory;
    @CCD(label = "Message history")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<Element<JudicialMessageReply>> judicialMessageReplies;
    @CCD(label = "Closure note", typeOverride = FieldType.TextArea)
    private final String closureNote;
    @CCD(label = "Sender's email address", typeOverride = FieldType.Email)
    private final String replyFrom;
    @CCD(label = "Recipient's email address", typeOverride = FieldType.Email)
    private final String replyTo;

    public String toLabel() {
        List<String> labels = new ArrayList<>();

        if (YES.equals(isRelatedToC2)) {
            labels.add("C2");
        }

        if (isNotBlank(getSubject())) {
            labels.add(getSubject());
        }

        labels.add(dateSent);

        if (isNotBlank(getUrgency())) {
            labels.add(getUrgency());
        }

        String label = String.join(", ", labels);
        return StringUtils.abbreviate(label, MAX_DYNAMIC_LIST_LABEL_LENGTH);
    }

    @JsonIgnore
    public boolean isFirstMessage() {
        String formattedLatestMessage = String.format("%s - %s", getSender(), latestMessage);
        return formattedLatestMessage.equals(messageHistory);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<div class='govuk-warning-text'><span class='govuk-warning-text__icon' aria-hidden='true'>!</span><strong class='govuk-warning-text__text'>Using this option will not auto-assign a task as they are not the hearing/allocated judge. After the task has been created, you must assign it manually on the Tasks tab to ensure it isn't missed.</strong></div>",
          showCondition = "otherJudgeWarningLabel=\"DO_NOT_SHOW\"",
          typeOverride = FieldType.Label
  )
  private String otherJudgeWarningLabel;
  @CCD(
          label = "This message will now be marked as closed",
          showCondition = "closeMessageLabel=\"DO_NOT_SHOW\"",
          typeOverride = FieldType.Label
  )
  private String closeMessageLabel;
  // ==== end synthesised definition-only fields ====
}
