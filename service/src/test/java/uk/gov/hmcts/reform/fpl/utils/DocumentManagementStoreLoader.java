package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Date;

public class DocumentManagementStoreLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private DocumentManagementStoreLoader() {
        // NO-OP
    }

    public static Document document() {
        Document.Links links = new Document.Links();
        links.self = new Document.Link();
        links.binary = new Document.Link();
        links.self.href = "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4";
        links.binary.href = "http://localhost/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary";
        return Document.builder()
            .size(72552)
            .mimeType("application/pdf")
            .originalDocumentName("file.pdf")
            .createdBy("15")
            .lastModifiedBy("15")
            .modifiedOn(new DateTime("2017-11-01T10:23:55.120+0000").toDate())
            .createdOn(new DateTime("2017-11-01T10:23:55.271+0000").toDate())
            .classification(Classification.PRIVATE)
            .links(links)
            .build();
//        return read("document-management-store-api/document.json", Document.class);
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
