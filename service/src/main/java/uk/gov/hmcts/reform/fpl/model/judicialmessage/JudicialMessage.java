package uk.gov.hmcts.reform.fpl.model.judicialmessage;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@ToString(callSuper = true)
public class JudicialMessage extends JudicialMessageMetaData {
    private static final int MAX_DYNAMIC_LIST_LABEL_LENGTH = 250;

    private final String dateSent;
    private final LocalDateTime updatedTime;
    private final JudicialMessageStatus status;
    private final List<Element<DocumentReference>> relatedDocuments;
    private final String relatedDocumentFileNames;
    private final YesNo isRelatedToC2;
    private final String isReplying;
    private final String latestMessage;
    private final String messageHistory;
    private final String closureNote;
    private final String replyFrom;
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
}
