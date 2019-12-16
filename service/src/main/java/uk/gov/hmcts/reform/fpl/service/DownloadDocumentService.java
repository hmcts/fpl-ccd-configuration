package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.net.URI;

import static java.lang.String.join;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @ __(@Autowired))
public class DownloadDocumentService {
    private final AuthTokenGenerator authTokenGenerator;
    private final DocumentDownloadClientApi documentDownloadClient;
    private final IdamApi idamApi;

    public byte[] downloadDocument(final String authorization, final String userId, final String documentUrlString) {
        final String userRoles = join(",", idamApi.retrieveUserInfo(authorization).getRoles());

        ResponseEntity<Resource> documentDownloadResponse = documentDownloadClient.downloadBinary(
            authorization, authTokenGenerator.generate(), userRoles, userId,
            URI.create(documentUrlString).getPath());

        if (isNotEmpty(documentDownloadResponse) && HttpStatus.OK == documentDownloadResponse.getStatusCode()) {
            ByteArrayResource resourceByte = (ByteArrayResource) documentDownloadResponse.getBody();
            return requireNonNull(resourceByte).getByteArray();
        } else {
            throw new IllegalArgumentException(String.format("Download of document from %s unsuccessful.",
                documentUrlString));
        }
    }
}
