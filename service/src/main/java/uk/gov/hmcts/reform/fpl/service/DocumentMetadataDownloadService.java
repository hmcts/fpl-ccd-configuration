package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.document.DocumentMetadataDownloadClientApi;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.net.URI;
import java.util.Optional;

import static java.lang.String.join;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentMetadataDownloadService {
    private final AuthTokenGenerator authTokenGenerator;
    private final DocumentMetadataDownloadClientApi documentMetadataDownloadClient;
    private final IdamClient idamClient;
    private final RequestData requestData;

    private final FeatureToggleService featureToggleService;
    private final SecureDocStoreService secureDocStoreService;

    public DocumentReference getDocumentMetadata(final String documentUrlString) {
        final String userRoles = join(",", idamClient.getUserInfo(requestData.authorisation()).getRoles());
        log.info("Download metadata for document  {} by user {} with roles {}",
                documentUrlString,
                requestData.userId(),
                userRoles);

        if (featureToggleService.isSecureDocstoreEnabled()) {
            Document document = secureDocStoreService.getDocumentMetadata(documentUrlString);

            return Optional.ofNullable(document)
                .map(doc -> DocumentReference.buildFromDocument(document)
                    .toBuilder()
                    .size(document.size)
                    .build())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.join(":",
                            "Download of meta data unsuccessful for document :",
                            documentUrlString)
                    )
                );
        } else {
            uk.gov.hmcts.reform.document.domain.Document document = documentMetadataDownloadClient.getDocumentMetadata(requestData.authorisation(),
                authTokenGenerator.generate(),
                userRoles,
                requestData.userId(),
                URI.create(documentUrlString).getPath()
            );

            return Optional.ofNullable(document)
                .map(doc -> DocumentReference.buildFromDocument(document)
                    .toBuilder()
                    .size(document.size)
                    .build())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.join(":",
                            "Download of meta data unsuccessful for document :",
                            documentUrlString)
                    )
                );
        }
    }
}
