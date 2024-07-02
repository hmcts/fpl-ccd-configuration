package uk.gov.hmcts.reform.fpl.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentReference {
    @JsonProperty("document_url")
    private final String url;
    @JsonProperty("document_filename")
    private String filename;
    @JsonProperty("document_binary_url")
    private final String binaryUrl;
    @JsonProperty("upload_timestamp")
    private final LocalDateTime uploadedTimestamp;
    @JsonIgnore
    private Long size;
    @JsonIgnore
    private String type;

    @Deprecated
    public static DocumentReference buildFromDocument(uk.gov.hmcts.reform.document.domain.Document document) {
        return DocumentReference.builder()
            .url(document.links.self.href)
            .binaryUrl(document.links.binary.href)
            .filename(document.originalDocumentName)
            .build();
    }

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
}
