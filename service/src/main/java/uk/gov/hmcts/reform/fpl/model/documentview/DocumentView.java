package uk.gov.hmcts.reform.fpl.model.documentview;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.LocalDateTime;

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
    private String title;
    private boolean includeSWETField;
    private boolean includeDocumentName;
    private LocalDateTime uploadedDateTime;

}
