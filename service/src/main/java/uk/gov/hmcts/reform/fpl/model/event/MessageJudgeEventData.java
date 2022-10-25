package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MessageJudgeEventData {
    Object additionalApplicationsDynamicList;
    Object judicialMessageDynamicList;
    String judicialMessageNote;
    JudicialMessageMetaData judicialMessageMetaData;
    String relatedDocumentsLabel;
    JudicialMessage judicialMessageReply;

    public static String[] transientFields() {
        return new String[]{
            "hasAdditionalApplications", "isMessageRegardingAdditionalApplications",
            "additionalApplicationsDynamicList", "relatedDocumentsLabel",
            "nextHearingLabel", "judicialMessageMetaData", "judicialMessageNote", "judicialMessageDynamicList",
            "judicialMessageReply", "replyToMessageJudgeNextHearingLabel"
        };
    }
}
