package uk.gov.hmcts.reform.fpl.service;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder
public class DocumentView {

    String uploadedBy;
    String uploadedAt;
    String type;
    DocumentReference document;
}
