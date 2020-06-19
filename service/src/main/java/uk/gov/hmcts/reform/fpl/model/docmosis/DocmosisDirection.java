package uk.gov.hmcts.reform.fpl.model.docmosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;

@Data
@Builder(builderClassName = "Builder")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocmosisDirection {
    public final String header;
    public final DirectionAssignee assignee;
    public final String title;
    public final String body;
}
