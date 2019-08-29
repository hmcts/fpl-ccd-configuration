package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DocumentSocialWorkOther {
    private final String documentTitle;
    private final DocumentReference typeOfDocument;
}
