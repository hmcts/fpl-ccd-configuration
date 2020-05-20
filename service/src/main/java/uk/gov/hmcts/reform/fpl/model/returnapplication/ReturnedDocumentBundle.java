package uk.gov.hmcts.reform.fpl.model.returnapplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder
@AllArgsConstructor
public class ReturnedDocumentBundle {
    private String submittedDate;
    private String returnedDate;
    private DocumentReference document;
}
