package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.enums.DocumentStatus;
import uk.gov.hmcts.reform.fpl.model.common.Document;

public class DocumentsHelper {

    private DocumentsHelper() {
        // No OP
    }

    public static boolean hasDocumentPresent(Document document) {
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
}
