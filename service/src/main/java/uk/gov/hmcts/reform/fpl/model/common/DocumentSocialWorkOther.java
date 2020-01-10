package uk.gov.hmcts.reform.fpl.model.common;

import ccd.sdk.types.ComplexType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@ComplexType(name = "OtherDocument")
public class DocumentSocialWorkOther {
    private final String documentTitle;
    private final DocumentReference typeOfDocument;
}
