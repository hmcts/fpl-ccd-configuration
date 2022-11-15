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
import uk.gov.hmcts.reform.idam.client.IdamClient;

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
    private final IdamClient idamClient;
    private final RequestData requestData;

    private final SecureDocStoreService secureDocStoreService;
    private final FeatureToggleService featureToggleService;
    private final SystemUserService systemUserService;

    public byte[] downloadDocument(final String documentUrlString) {

        if (featureToggleService.isSecureDocstoreEnabled()) {
            return secureDocStoreService.downloadDocument(documentUrlString);
        } else {
            String userRoles = "caseworker-publiclaw-systemupdate";
            boolean useSystemUser = false;
            try {
                userRoles = join(",", idamClient.getUserInfo(requestData.authorisation()).getRoles());
            } catch (IllegalStateException e) {
                // TODO - Remove this after cafcass resend job
                log.info("Outside of a request, use system user");
                useSystemUser = true;
            }

            String auth = useSystemUser ? systemUserService.getSysUserToken() : requestData.authorisation();
            String userId = useSystemUser ? systemUserService.getUserId(auth) : requestData.userId();

            log.info("Download document {} by user {} with roles {}", documentUrlString, userId,
                userRoles);

            ResponseEntity<Resource> documentDownloadResponse =
                documentDownloadClient.downloadBinary(auth,
                    authTokenGenerator.generate(),
                    userRoles,
                    userId,
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
}
