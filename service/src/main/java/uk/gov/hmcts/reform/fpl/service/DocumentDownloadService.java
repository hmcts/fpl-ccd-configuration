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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.fpl.exceptions.EmptyFileException;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.net.URI;
import java.util.Optional;

import static java.lang.String.join;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentDownloadService {
    private final AuthTokenGenerator authTokenGenerator;
    private final DocumentDownloadClientApi documentDownloadClient;
    private final IdamApi idamApi;
    private final RequestData requestData;

    public byte[] downloadDocument(final String documentUrlString) {
        final String userRoles = join(",", idamApi.retrieveUserInfo(requestData.authorisation()).getRoles());

        ResponseEntity<Resource> documentDownloadResponse =
            documentDownloadClient.downloadBinary(requestData.authorisation(),
            authTokenGenerator.generate(),
            userRoles,
            requestData.userId(),
            URI.create(documentUrlString).getPath());

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
}
