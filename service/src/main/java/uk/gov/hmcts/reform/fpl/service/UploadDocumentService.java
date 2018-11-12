package uk.gov.hmcts.reform.fpl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import static com.google.common.collect.Lists.newArrayList;

@Service
public class UploadDocumentService {

    @Autowired
    private DocumentUploadClientApi documentUploadClient;

    public Document uploadDocument(String userId, String authorization,
                                   String serviceAuthorization, byte[] pdfDocument, String fileName) {
        MultipartFile file = new InMemoryMultipartFile("files", fileName, MediaType.APPLICATION_PDF_VALUE, pdfDocument);

        UploadResponse response = documentUploadClient.upload(authorization,
            serviceAuthorization, userId, newArrayList(file));

        return response.getEmbedded().getDocuments().stream()
            .findFirst()
            .orElseThrow(() ->
                new RuntimeException("Document management failed uploading"));
    }
}
