package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.enums.MessageRegardingDocuments;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessageMetaData;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MessageJudgeEventData {
    DynamicList additionalApplicationsDynamicList;
    Object judicialMessageDynamicList;
    String judicialMessageNote;
    JudicialMessageMetaData judicialMessageMetaData;
    String relatedDocumentsLabel;
    JudicialMessage judicialMessageReply;
    YesNo isJudiciary;
    DynamicList documentTypesDynamicList;
    DynamicList documentDynamicList;
    MessageRegardingDocuments isMessageRegardingDocuments;

    public static String[] transientFields() {
        return new String[]{
            "hasAdditionalApplications", "isMessageRegardingDocuments", "additionalApplicationsDynamicList",
            "documentTypesDynamicList", "documentDynamicList", "relatedDocumentsLabel","attachDocumentLabel",
            "nextHearingLabel", "judicialMessageMetaData", "judicialMessageNote", "judicialMessageDynamicList",
            "judicialMessageReply", "replyToMessageJudgeNextHearingLabel", "isJudiciary"
        };
    }
}
