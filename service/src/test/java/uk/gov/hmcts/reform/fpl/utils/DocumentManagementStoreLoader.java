package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;

import java.io.IOException;
import java.io.UncheckedIOException;

public class DocumentManagementStoreLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private DocumentManagementStoreLoader() {
        // NO-OP
    }

    public static Document document() {
        return read("document-management-store-api/document.json", Document.class);
    }

    public static Document c6Document() {
        return read("document-management-store-api/c6Document.json", Document.class);
    }

    public static UploadResponse successfulDocumentUploadResponse() {
        return read("document-management-store-api/responses/upload-success.json", UploadResponse.class);
    }

    public static UploadResponse unsuccessfulDocumentUploadResponse() {
        return read("document-management-store-api/responses/upload-failure.json", UploadResponse.class);
    }

    private static <T> T read(String path, Class<T> clazz) {
        String response = ResourceReader.readString(path);
        try {
            return mapper.readValue(response, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
