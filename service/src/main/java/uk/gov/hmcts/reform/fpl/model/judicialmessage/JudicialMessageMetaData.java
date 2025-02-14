package uk.gov.hmcts.reform.fpl.model.judicialmessage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageRoleType;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

@Data
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JudicialMessageMetaData {
    private final JudicialMessageRoleType senderType;
    private final String sender;
    private final JudicialMessageRoleType recipientType;
    private final DynamicList recipientDynamicList;
    private final String recipient;
    @JsonProperty("requestedBy")
    private final String subject;
    private final String urgency;
    private final String applicationType;
}
