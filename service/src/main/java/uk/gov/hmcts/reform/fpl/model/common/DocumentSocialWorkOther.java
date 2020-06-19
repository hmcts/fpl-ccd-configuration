package uk.gov.hmcts.reform.fpl.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.types.ComplexType;

@Data
@Builder
@AllArgsConstructor
@ComplexType(name = "OtherDocument")
public class DocumentSocialWorkOther {
    private final String documentTitle;
    private final DocumentReference typeOfDocument;
}
