package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @ __(@Autowired))
public class DownloadDocumentService {
    private final AuthTokenGenerator authTokenGenerator;
    private final DocumentDownloadClientApi documentDownloadClient;
    private final IdamApi idamApi;

    public Resource downloadDocumentResource(final String authorisation, final String userId,
                                             final String documentDownloadUri) {
        final String userRoles = idamApi.retrieveUserInfo(authorisation).getRoles().toString();

        ResponseEntity<Resource> documentDownloadResponse = documentDownloadClient.downloadBinary(authorisation,
            authTokenGenerator.generate(), userRoles, userId, documentDownloadUri);

        if (documentDownloadResponse.getStatusCode() == HttpStatus.OK) {
            return documentDownloadResponse.getBody();
        } else {
            log.error("Download of document from {} unsuccessful due to a {}", documentDownloadUri,
                documentDownloadResponse.getStatusCodeValue());
            return null;
        }
    }
}
