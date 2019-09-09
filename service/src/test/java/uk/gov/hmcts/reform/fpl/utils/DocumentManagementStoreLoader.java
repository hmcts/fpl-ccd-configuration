package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;

import java.io.IOException;

public class DocumentManagementStoreLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private DocumentManagementStoreLoader() {
        // NO-OP
    }

    public static Document document() throws IOException {
        String response = ResourceReader.readString("document-management-store-api/document.json");
        return mapper.readValue(response, Document.class);
    }

    public static Document c6Document() throws IOException {
        String response = ResourceReader.readString("document-management-store-api/c6Document.json");
        return mapper.readValue(response, Document.class);
    }

    public static UploadResponse successfulDocumentUploadResponse() throws IOException {
        String response = ResourceReader.readString("document-management-store-api/responses/upload-success.json");
        return mapper.readValue(response, UploadResponse.class);
    }

    public static UploadResponse unsuccessfulDocumentUploadResponse() throws IOException {
        String response = ResourceReader.readString("document-management-store-api/responses/upload-failure.json");
        return mapper.readValue(response, UploadResponse.class);
    }
}
