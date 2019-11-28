package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;
import java.util.UUID;

//TODO: deal with all respondingOnBehalfOf parties

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DirectionResponse {
    private final UUID directionId;
    private final DirectionAssignee assignee;
    private final String respondingOnBehalfOf;
    private final String complied;
    private final String documentDetails;
    private final DocumentReference file;
    private final String cannotComplyReason;
    private final List<String> c2Uploaded;
    private final DocumentReference cannotComplyFile;
}
