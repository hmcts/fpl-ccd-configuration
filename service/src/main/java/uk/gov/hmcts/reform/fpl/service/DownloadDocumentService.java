package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.net.URI;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @ __(@Autowired))
public class DownloadDocumentService {
    private final AuthTokenGenerator authTokenGenerator;
    private final DocumentDownloadClientApi documentDownloadClient;
    private final IdamApi idamApi;

    public byte[] downloadDocument(final String authorisation, final String userId, final String documentUrlString) {
        final String userRoles = String.join(",", idamApi.retrieveUserInfo(authorisation).getRoles());

        ResponseEntity<Resource> documentDownloadResponse = documentDownloadClient.downloadBinary(authorisation,
            authTokenGenerator.generate(), userRoles, userId, URI.create(documentUrlString).getPath());

        if (OK.equals(documentDownloadResponse.getStatusCode())) {
            ByteArrayResource resourceByte = (ByteArrayResource) documentDownloadResponse.getBody();
            return requireNonNull(resourceByte).getByteArray();
        } else {
            throw new IllegalArgumentException(String.format(
                "Download of document from %s unsuccessful with a %s error. More details %s", documentUrlString,
                documentDownloadResponse.getStatusCodeValue(), documentDownloadResponse.getBody()));
        }
    }
}
