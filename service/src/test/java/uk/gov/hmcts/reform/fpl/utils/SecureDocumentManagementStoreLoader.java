package uk.gov.hmcts.reform.fpl.utils;

import org.joda.time.DateTime;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;

import java.util.List;
import java.util.Map;

public class SecureDocumentManagementStoreLoader {

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
    }

    public static UploadResponse successfulDocumentUploadResponse() {
        Document.Links links = new Document.Links();
        links.self = new Document.Link();
        links.binary = new Document.Link();
        links.self.href = "http://localhost:4455/cases/documents/46f068e9-a395-49b3-a819-18e8c1327f11";
        links.binary.href = "http://localhost:4455/cases/documents/46f068e9-a395-49b3-a819-18e8c1327f11/binary";

        Document doc = Document.builder()
            .size(1221560)
            .mimeType("image/png")
            .originalDocumentName("Screenshot 2020-02-17 at 12.51.46.png")
            .createdOn(new DateTime("2020-04-09T10:53:36+0000").toDate())
            .classification(Classification.PUBLIC)
            .metadata(Map.of("caseTypeId", "abc", "jurisdictionId", "abc"))
            .ttl(new DateTime("2020-04-09T11:03:36+0000").toDate())
            .links(links)
            .hashToken("9111b07b8a41347b37e96c1cac18113e1c3855687beb095eeb53477fae69b1f7")
            .build();
        return new UploadResponse(List.of(doc));
    }

    public static UploadResponse unsuccessfulDocumentUploadResponse() {
        return new UploadResponse(List.of());
    }

}
