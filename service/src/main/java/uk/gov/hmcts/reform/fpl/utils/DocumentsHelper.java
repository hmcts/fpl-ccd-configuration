package uk.gov.hmcts.reform.fpl.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import uk.gov.hmcts.reform.fpl.enums.DocumentStatus;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.UUID;

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

    public static boolean hasExtension(DocumentReference document, String extension) {
        return hasExtension(document.getFilename(), extension);
    }

    public static boolean hasExtension(String filename, String extension) {
        return extension.equalsIgnoreCase(FilenameUtils.getExtension(filename));
    }

    public static String updateExtension(String filename, String newExtension) {
        if (!hasExtension(filename, newExtension)) {
            return FilenameUtils.removeExtension(filename).concat("." + newExtension);
        }
        return filename;
    }

    public static UUID getDocumentIdFromUrl(final String documentUrlString) {
        String selfHref = documentUrlString.replace("/binary", "");
        UUID documentId = UUID.fromString(selfHref.substring(selfHref.length() - 36));

        return documentId;
    }
}
