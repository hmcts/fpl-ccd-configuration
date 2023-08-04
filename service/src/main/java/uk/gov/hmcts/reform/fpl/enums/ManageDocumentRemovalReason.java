package uk.gov.hmcts.reform.fpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ManageDocumentRemovalReason {
    MISTAKE_ON_DOCUMENT("There is a mistake on the document"),
    UPLOADED_TO_WRONG_CASE("The document was uploaded to the wrong case"),
    OTHER("Another reason");

    private String description;
}
