package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import static com.google.common.collect.Lists.newArrayList;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadDocumentService {
    private final AuthTokenGenerator authTokenGenerator;
    private final DocumentUploadClientApi documentUploadClient;
    private final RequestData requestData;

    public Document uploadPDF(byte[] pdf, String fileName) {
        MultipartFile file = new InMemoryMultipartFile("files", fileName, MediaType.APPLICATION_PDF_VALUE, pdf);

        UploadResponse response = documentUploadClient.upload(requestData.authorisation(),
            authTokenGenerator.generate(), requestData.userId(), newArrayList(file));

        Document document = response.getEmbedded().getDocuments().stream()
            .findFirst()
            .orElseThrow(() ->
                new RuntimeException("Document upload failed due to empty result"));

        log.debug("Document upload resulted with links: {}, {}", document.links.self.href, document.links.binary.href);

        return document;
    }
}
