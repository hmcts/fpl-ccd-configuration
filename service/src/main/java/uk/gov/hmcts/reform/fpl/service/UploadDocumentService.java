package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import static com.launchdarkly.shaded.com.google.common.collect.Lists.newArrayList;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadDocumentService {
    private final AuthTokenGenerator authTokenGenerator;
    private final DocumentUploadClientApi documentUploadClient;
    private final RequestData requestData;

    private final SecureDocStoreService secureDocStoreService;

    // REFACTOR: 08/04/2021 Remove this method in subsequent PR
    public Document uploadPDF(byte[] pdf, String fileName) {
        return uploadDocument(pdf, fileName, MediaType.APPLICATION_PDF_VALUE);
    }

    public Document uploadDocument(byte[] pdf, String fileName, String contentType) {

        if (secureDocStoreService.enabled) {
            return secureDocStoreService.uploadDocument(pdf, fileName, contentType);
        } else {
            MultipartFile file = new InMemoryMultipartFile("files", fileName, contentType, pdf);

            UploadResponse response = documentUploadClient.upload(requestData.authorisation(),
                authTokenGenerator.generate(), requestData.userId(), newArrayList(file));

            uk.gov.hmcts.reform.document.domain.Document document = response.getEmbedded().getDocuments().stream()
                .findFirst()
                .orElseThrow(() ->
                    new RuntimeException("Document upload failed due to empty result"));
            log.debug("Document upload resulted with links: {}, {}", document.links.self.href,
                document.links.binary.href);

            return oldToSecureDocument(document);
        }
    }

    public static Document oldToSecureDocument(uk.gov.hmcts.reform.document.domain.Document document) {
        Document.Links links = new Document.Links();
        links.binary = new Document.Link();
        links.self = new Document.Link();
        links.binary.href = document.links.binary.href;
        links.self.href = document.links.self.href;

        return Document.builder()
            .classification(Classification.valueOf(document.classification))
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
