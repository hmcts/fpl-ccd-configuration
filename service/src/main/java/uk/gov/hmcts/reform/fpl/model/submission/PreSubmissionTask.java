package uk.gov.hmcts.reform.fpl.model.submission;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.Event;

import java.util.List;

@Data
@Builder
public class PreSubmissionTask {
    private final Event event;
    private final List<String> messages;
}
