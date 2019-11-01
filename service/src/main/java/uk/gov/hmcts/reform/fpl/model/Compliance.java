package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
// to be removed
@SuppressWarnings("MemberName")
public class Compliance {
    private final DirectionAssignee assignee;
    private final String complied;
    private final DocumentReference file;
    private final String documentDetails;
    private final String cannotComply_reason;
    private final String c2Uploaded;
    private final DocumentReference supportingFile;
    private final String supportingDocumentDetails;
}
