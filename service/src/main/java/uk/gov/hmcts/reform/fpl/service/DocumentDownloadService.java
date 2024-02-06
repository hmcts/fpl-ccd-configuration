package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.utils.SecureDocStoreHelper;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentDownloadService {
    private final SecureDocStoreService secureDocStoreService;
    private final FeatureToggleService featureToggleService;

    public byte[] downloadDocument(final String documentUrlString) {
        return new SecureDocStoreHelper(secureDocStoreService, featureToggleService).download(documentUrlString,
            secureDocStoreService.downloadDocument(documentUrlString));
    }
}
