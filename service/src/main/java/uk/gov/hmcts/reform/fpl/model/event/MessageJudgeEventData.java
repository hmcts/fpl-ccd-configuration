package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.MessageJudgeOptions;
import uk.gov.hmcts.reform.fpl.model.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageMetaData;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MessageJudgeEventData {
    Object c2DynamicList;
    Object judicialMessageDynamicList;
    String judicialMessageNote;
    JudicialMessageMetaData judicialMessageMetaData;
    String relatedDocumentsLabel;
    MessageJudgeOptions messageJudgeOption;
    JudicialMessage judicialMessageReply;

    public static String[] transientFields() {
        return new String[]{
            "hasC2Applications", "isMessageRegardingC2", "c2DynamicList", "relatedDocumentsLabel", "nextHearingLabel",
            "judicialMessageMetaData", "judicialMessageNote"
        };
    }
}
