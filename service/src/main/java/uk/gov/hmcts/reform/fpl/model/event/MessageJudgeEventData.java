package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.MessageJudgeOptions;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;

import static uk.gov.hmcts.reform.fpl.enums.MessageJudgeOptions.REPLY;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MessageJudgeEventData {
    Object additionalApplicationsDynamicList;
    Object judicialMessageDynamicList;
    String judicialMessageNote;
    JudicialMessageMetaData judicialMessageMetaData;
    String relatedDocumentsLabel;
    MessageJudgeOptions messageJudgeOption;
    JudicialMessage judicialMessageReply;

    public static String[] transientFields() {
        return new String[] {
            "hasAdditionalApplications", "isMessageRegardingAdditionalApplications",
            "additionalApplicationsDynamicList", "relatedDocumentsLabel",
            "nextHearingLabel", "judicialMessageMetaData", "judicialMessageNote", "judicialMessageDynamicList",
            "messageJudgeOption", "judicialMessageReply", "hasJudicialMessages"
        };
    }

    public boolean isReplyingToAMessage() {
        return REPLY.equals(messageJudgeOption);
    }
}
