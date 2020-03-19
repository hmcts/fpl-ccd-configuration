package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.types.CCD;
import uk.gov.hmcts.ccd.sdk.types.ComplexType;
import uk.gov.hmcts.reform.document.domain.Document;

@Data
@Builder(toBuilder = true)
@ComplexType(name = "Document", generate = false)
public class DocumentReference {
    @JsonProperty("document_url")
    private final String url;
    @JsonProperty("document_filename")
    private final String filename;
    @JsonProperty("document_binary_url")
    private final String binaryUrl;

    public static DocumentReference buildFromDocument(Document document) {
        return DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .build();
    }
}
