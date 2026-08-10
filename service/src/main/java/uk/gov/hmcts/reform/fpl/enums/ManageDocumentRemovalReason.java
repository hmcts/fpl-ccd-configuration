package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@AllArgsConstructor
public enum ManageDocumentRemovalReason {
    @CCD(label = "There is a mistake on the document")
    MISTAKE_ON_DOCUMENT("There is a mistake on the document"),
    @CCD(label = "The document was uploaded to the wrong case")
    UPLOADED_TO_WRONG_CASE("The document was uploaded to the wrong case"),
    @CCD(label = "Another reason")
    OTHER("Another reason");

    private String description;
}
