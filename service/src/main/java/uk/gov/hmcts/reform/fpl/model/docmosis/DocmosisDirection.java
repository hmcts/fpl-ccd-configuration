package uk.gov.hmcts.reform.fpl.model.docmosis;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;

@Data
@Builder
public class DocmosisDirection {
    public final DirectionAssignee assignee;
    public final String title;
    public final String body;
}
