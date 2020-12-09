package uk.gov.hmcts.reform.fpl.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.fpl.model.JudicialMessageMetaData;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MessageJudgeEventData {
    Object c2DynamicList;
    String judicialMessageNote;
    JudicialMessageMetaData judicialMessageMetaData;
    String relatedDocumentsLabel;
}
