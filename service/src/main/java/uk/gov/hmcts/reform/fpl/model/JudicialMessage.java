package uk.gov.hmcts.reform.fpl.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.fpl.enums.JudicialMessageStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Jacksonized
@ToString(callSuper = true)
public class JudicialMessage extends JudicialMessageMetaData {
    private final String dateSent;
    private final LocalDateTime dateSentAsLocalDateTime;
    private final String note;
    private final JudicialMessageStatus status;
    private final List<Element<DocumentReference>> relatedDocuments;
    private final YesNo isRelatedToC2;

    public String toLabel() {
        List<String> labels = new ArrayList<>();

        if (hasRelatedDocuments()) {
            labels.add("C2");
        }

        labels.add(note);
        labels.add(dateSent);

        return String.join(",", labels);
    }

    private boolean hasRelatedDocuments() {
        return YES.equals(isRelatedToC2);
    }
}
