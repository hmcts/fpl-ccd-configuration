package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.io.FilenameUtils;
import uk.gov.hmcts.reform.document.domain.Document;

@Data
@Builder(toBuilder = true)
public class DocumentReference {
    @JsonProperty("document_url")
    private final String url;
    @JsonProperty("document_filename")
    private String filename;
    @JsonProperty("document_binary_url")
    private final String binaryUrl;

    public static DocumentReference buildFromDocument(Document document) {
        return DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .build();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return url == null && filename == null && binaryUrl == null;
    }

    @JsonIgnore
    public boolean hasExtensionTypeOf(String documentExtension) {
        return documentExtension.equals(FilenameUtils.getExtension(filename));
    }
}
