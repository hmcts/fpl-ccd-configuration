package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.document.domain.Classification;
import uk.gov.hmcts.reform.fpl.exceptions.EmptyFileException;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.util.Optional;
import java.util.UUID;

import static com.launchdarkly.shaded.com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.getDocumentIdFromUrl;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SecureDocStoreService {

    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDocumentClientApi caseDocumentClientApi;
    private final RequestData requestData;

    public Document uploadDocument(byte[] pdf, String fileName, String contentType) {

        MultipartFile file = new InMemoryMultipartFile("files", fileName, contentType, pdf);

        DocumentUploadRequest request = new DocumentUploadRequest(Classification.RESTRICTED.toString(),
            "CARE_SUPERVISION_EPO", "PUBLICLAW", newArrayList(file));

        UploadResponse response = caseDocumentClientApi.uploadDocuments(requestData.authorisation(),
            authTokenGenerator.generate(), request);

        Document document = response.getDocuments().stream()
            .findFirst()
            .orElseThrow(() ->
                new RuntimeException("Document upload failed due to empty result"));

        log.debug("Document upload resulted with links: {}, {}", document.links.self.href, document.links.binary.href);

        return document;
    }

    public byte[] downloadDocument(final String documentUrlString) {
        return downloadDocument(documentUrlString, requestData.authorisation());
    }

    public byte[] downloadDocument(final String documentUrlString, String authorisation) {
        UUID documentId = getDocumentIdFromUrl(documentUrlString);
        ResponseEntity<Resource> documentDownloadResponse = caseDocumentClientApi.getDocumentBinary(
            authorisation, authTokenGenerator.generate(), documentId);

        if (isNotEmpty(documentDownloadResponse) && HttpStatus.OK == documentDownloadResponse.getStatusCode()) {
            return Optional.of(documentDownloadResponse)
                .map(HttpEntity::getBody)
                .map(ByteArrayResource.class::cast)
                .map(ByteArrayResource::getByteArray)
                .orElseThrow(EmptyFileException::new);
        }
        throw new IllegalArgumentException(String.format("Download of document from %s unsuccessful.",
            documentUrlString));

    }

    public Document getDocumentMetadata(final String documentUrlString) {
        UUID documentId = getDocumentIdFromUrl(documentUrlString);
        return caseDocumentClientApi.getMetadataForDocument(
            requestData.authorisation(),
            authTokenGenerator.generate(),
            documentId
        );
    }
}
