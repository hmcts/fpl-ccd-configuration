package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.fpl.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.exceptions.DocumentException;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static com.google.common.collect.Lists.newArrayList;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadDocumentService {
    private final AuthTokenGenerator authTokenGenerator;
    private final DocumentUploadClientApi documentUploadClient;
    private final RequestData requestData;
    private final IdamClient idamClient;
    private final SystemUpdateUserConfiguration userConfig;
    private final DocumentMetadataDownloadClientApi documentMetadataDownloadClient;

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

    public String getUploadedDocumentUserInfo(String documentPath) {
        String userToken = idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        Document document = getDocumentMetadata(documentPath, userToken);

        UserDetails userDetails = idamClient.getUserByUserId(userToken, document.createdBy);
        boolean isHMCTSUser = userDetails.getRoles().stream().anyMatch(UserRole::isHMCTSUser);

        return isHMCTSUser ? "HMCTS": userDetails.getEmail();
    }

    public Document getDocumentMetadata(String documentPath, String userToken) {
        try {
            UserDetails userDetails = idamClient.getUserByUserId(userToken, requestData.userId());
            String userRoles = String.join(",", userDetails.getRoles());

            return documentMetadataDownloadClient.getDocumentMetadata(
                userToken,
                authTokenGenerator.generate(),
                userRoles,
                userDetails.getId(),
                documentPath
            );
        } catch (Exception e) {
            throw new DocumentException("Unable to get document metadata");
        }
    }
}
