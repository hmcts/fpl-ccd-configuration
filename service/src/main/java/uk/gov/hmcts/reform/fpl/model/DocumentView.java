package uk.gov.hmcts.reform.fpl.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder
@Getter
public class DocumentView {
    private String uploadedBy;
    private String uploadedAt;
    private String type;
    private String includedInSWET;
    private String documentName;
    private String fileName;
    private boolean confidential;
    private DocumentReference document;
}
