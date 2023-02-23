package uk.gov.hmcts.reform.fpl.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.SecureDocStoreService;

import java.util.concurrent.Callable;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
public class SecureDocStoreHelper {

    private FeatureToggleService featureToggleService;
    private SecureDocStoreService secureDocStoreService;
    private String documentUrlString;

    private SecureDocStoreHelper(SecureDocStoreService secureDocStoreService,
                                 FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
        this.secureDocStoreService = secureDocStoreService;
    }

    public static SecureDocStoreHelper of(SecureDocStoreService secureDocStoreService,
                                          FeatureToggleService featureToggleService) {
        return new SecureDocStoreHelper(secureDocStoreService, featureToggleService);
    }

    public byte[] download(final String documentUrlString) {
        return download(documentUrlString, null);
    }

    @SneakyThrows
    public byte[] download(final String documentUrlString, Callable<byte[]> oldDmStoreApproach) {
        try {
            log.info(String.format("Downloading document: %s", documentUrlString));
            byte[] bytesFromSecureDocStore = secureDocStoreService.downloadDocument(documentUrlString);
            if (featureToggleService.isSecureDocstoreEnabled()) {
                return bytesFromSecureDocStore;
            }
        } catch (Exception t) {
            if (!featureToggleService.isSecureDocstoreEnabled()) {
                log.error("↑ ↑ ↑ ↑ ↑ ↑ ↑ EXCEPTION CAUGHT (SECURE DOC STORE: DISABLED) ↑ ↑ ↑ ↑ ↑ ↑ ↑", t);
            } else if (oldDmStoreApproach == null) {
                throw t;
            }
        }
        if (!featureToggleService.isSecureDocstoreEnabled() && !isEmpty(oldDmStoreApproach)) {
            log.info(String.format("Using old dm-store approach to download the document (%s).", documentUrlString));
            return oldDmStoreApproach.call();
        }
        throw new UnsupportedOperationException();
    }
}
