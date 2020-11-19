package uk.gov.hmcts.reform.fpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.fpl.enums.DocumentType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

@Data
@Builder
@AllArgsConstructor
public class SupportingDocument {
    private final DocumentReference document;
    private final DocumentType documentType;
}
