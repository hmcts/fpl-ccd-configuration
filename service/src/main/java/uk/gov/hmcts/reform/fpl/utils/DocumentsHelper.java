package uk.gov.hmcts.reform.fpl.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.fpl.enums.DocumentStatus;
import uk.gov.hmcts.reform.fpl.model.common.Document;

import java.net.URI;
import java.net.URISyntaxException;
@Slf4j
public class DocumentsHelper {

    private DocumentsHelper() {
        // No OP
    }

    private static boolean hasDocumentPresent(Document document) {
        return document != null;
    }

    public static boolean hasDocumentStatusSet(Document document) {
        return hasDocumentPresent(document) && document.getDocumentStatus() != null;
    }

    public static boolean hasDocumentStatusOf(Document document, DocumentStatus documentStatus) {
        return hasDocumentStatusSet(document) && document.getDocumentStatus().equals(documentStatus.getLabel());
    }

    public static boolean hasDocumentUploaded(Document document) {
        return hasDocumentPresent(document) && document.getTypeOfDocument() != null;
    }

    public static String concatGatewayConfigurationUrlAndMostRecentUploadedDocumentPath(final String mostRecentUploadedDocument, String url) {
        try {
            URI uri = new URI(mostRecentUploadedDocument);
            return url + uri.getPath();
        } catch (URISyntaxException e) {
            log.error(mostRecentUploadedDocument + " url incorrect.", e);
        }
        return "";
    }
}
