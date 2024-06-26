package uk.gov.hmcts.reform.fpl.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.SecureDocStoreService;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
public class SecureDocStoreHelper {

    private FeatureToggleService featureToggleService;
    private SecureDocStoreService secureDocStoreService;

    public SecureDocStoreHelper(SecureDocStoreService secureDocStoreService,
                                FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
        this.secureDocStoreService = secureDocStoreService;
    }

    public byte[] download(final String documentUrlString) {
        return download(documentUrlString, null);
    }

    /**
     * If secure doc store toggle is off, attempt to fire the API and log any exceptions caught.
     * @param documentUrlString  document url in string
     * @param oldDmStoreApproach if featureToggleService.isSecureDocstoreEnabled() = false, it is a mandatory parameter.
     *                           otherwise, UnsupportedOperationException will be thrown.
     * @return byte array of the file
     */
    @Retryable(value = UnsupportedOperationException.class, maxAttempts = 5, backoff = @Backoff(delay = 500))
    @SneakyThrows
    public byte[] download(final String documentUrlString, Callable<byte[]> oldDmStoreApproach) {
        try {
            log.info("Downloading document: {}", documentUrlString);
            byte[] bytesFromSecureDocStore = secureDocStoreService.downloadDocument(documentUrlString);
            if (featureToggleService.isSecureDocstoreEnabled()) {
                return bytesFromSecureDocStore;
            }
        } catch (Exception t) {
            if (!featureToggleService.isSecureDocstoreEnabled()) {
                log.error("↑ ↑ ↑ ↑ ↑ ↑ ↑ EXCEPTION CAUGHT (SECURE DOC STORE: DISABLED) ↑ ↑ ↑ ↑ ↑ ↑ ↑", t);
            } else {
                log.error("↑ ↑ ↑ ↑ ↑ ↑ ↑ EXCEPTION CAUGHT (SECURE DOC STORE: ENABLED) ↑ ↑ ↑ ↑ ↑ ↑ ↑", t);
                if (oldDmStoreApproach == null) {
                    throw t;
                }
            }
        }
        if (!featureToggleService.isSecureDocstoreEnabled() && !isEmpty(oldDmStoreApproach)) {
            return oldDmStoreApproach.call();
        }
        throw new UnsupportedOperationException();
    }

    private static Supplier<IllegalArgumentException> exceptionSupplier(String documentUrlString) {
        return () -> new IllegalArgumentException(
            String.join(":",
                "Download of meta data unsuccessful for document :",
                documentUrlString)
        );
    }

    public static DocumentReference convertToDocumentReference(String documentUrlString,
                                                         uk.gov.hmcts.reform.document.domain.Document document) {
        return Optional.ofNullable(document)
            .map(doc -> DocumentReference.buildFromDocument(document)
                .toBuilder()
                .size(document.size)
                .build())
            .orElseThrow(exceptionSupplier(documentUrlString));
    }

    public static DocumentReference convertToDocumentReference(String documentUrlString, Document document) {
        return Optional.ofNullable(document)
            .map(doc -> DocumentReference.buildFromDocument(document)
                .toBuilder()
                .size(document.size)
                .build())
            .orElseThrow(exceptionSupplier(documentUrlString));
    }

    public DocumentReference getDocumentMetadata(String documentUrlString) {
        return getDocumentMetadata(documentUrlString, null);
    }

    /**
     * If secure doc store toggle is off, attempt to fire the API and log any exceptions caught.
     * @param documentUrlString  document URL in string
     * @param oldDmStoreApproach if featureToggleService.isSecureDocstoreEnabled() = false, it is a mandatory parameter.
     * @return byte array of the file
     */
    @SneakyThrows
    public DocumentReference getDocumentMetadata(final String documentUrlString,
                                                 Callable<DocumentReference> oldDmStoreApproach) {
        Document document = null;
        try {
            log.info("Downloading document meta data: {}", documentUrlString);

            document = secureDocStoreService.getDocumentMetadata(documentUrlString);
        } catch (Exception t) {
            if (!featureToggleService.isSecureDocstoreEnabled()) {
                log.error("↑ ↑ ↑ ↑ ↑ ↑ ↑ EXCEPTION CAUGHT WHEN DOWNLOADING METADATA (SECURE DOC STORE: DISABLED)"
                    + " ↑ ↑ ↑ ↑ ↑ ↑ ↑", t);
            } else if (oldDmStoreApproach == null) {
                throw t;
            }
        }

        if (featureToggleService.isSecureDocstoreEnabled()) {
            return convertToDocumentReference(documentUrlString, document);
        }

        if (!featureToggleService.isSecureDocstoreEnabled() && !isEmpty(oldDmStoreApproach)) {
            return oldDmStoreApproach.call();
        }
        throw new UnsupportedOperationException();
    }

    public Document uploadDocument(byte[] byteArrayOfFile, String fileName, String contentType) {
        return uploadDocument(byteArrayOfFile, fileName, contentType, null);
    }

    /**
     * If secure doc store toggle is off, attempt to fire the API and log any exceptions caught.
     * @param byteArrayOfFile byte array of the file
     * @param fileName file name
     * @param contentType content type of the file
     * @param oldDmStoreApproach if featureToggleService.isSecureDocstoreEnabled() = false, it is a mandatory parameter.
     * @return Secure document object
     */
    @SneakyThrows
    public Document uploadDocument(byte[] byteArrayOfFile, String fileName, String contentType,
                                   Callable<Document> oldDmStoreApproach) {
        try {
            log.info("Uploading document file name: {} ({})", fileName, contentType);
            Document ret = secureDocStoreService.uploadDocument(byteArrayOfFile, fileName, contentType);
            if (featureToggleService.isSecureDocstoreEnabled()) {
                return ret;
            }
        } catch (Exception t) {
            if (!featureToggleService.isSecureDocstoreEnabled()) {
                log.error("↑ ↑ ↑ ↑ ↑ ↑ ↑ EXCEPTION CAUGHT WHEN UPLOADING DOCUMENT (SECURE DOC STORE: DISABLED)"
                    + " ↑ ↑ ↑ ↑ ↑ ↑ ↑", t);
            } else if (oldDmStoreApproach == null) {
                throw t;
            }
        }

        if (!featureToggleService.isSecureDocstoreEnabled() && !isEmpty(oldDmStoreApproach)) {
            return oldDmStoreApproach.call();
        }
        throw new UnsupportedOperationException();
    }

    public static Document oldToSecureDocument(uk.gov.hmcts.reform.document.domain.Document document) {
        Document.Links links = new Document.Links();
        links.binary = new Document.Link();
        links.self = new Document.Link();
        links.binary.href = document.links.binary.href;
        links.self.href = document.links.self.href;

        return Document.builder()
            .classification(Classification.valueOf(Optional.ofNullable(document.classification)
                .orElse(Classification.RESTRICTED.name())))
            .size(document.size)
            .mimeType(document.mimeType)
            .originalDocumentName(document.originalDocumentName)
            .createdOn(document.createdOn)
            .modifiedOn(document.modifiedOn)
            .createdBy(document.createdBy)
            .links(links)
            .build();
    }
}
