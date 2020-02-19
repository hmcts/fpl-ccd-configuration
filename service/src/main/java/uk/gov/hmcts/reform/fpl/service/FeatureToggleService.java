package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.client.LDClient;
import com.launchdarkly.client.LDUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FeatureToggleService {

    private final LDClient ldClient;
    private final LDUser ldUser;

    @Autowired
    public FeatureToggleService(LDClient ldClient, @Value("${ld.user_key}") String ldUserKey) {
        this.ldClient = ldClient;
        this.ldUser = new LDUser.Builder(ldUserKey)
            .custom("timestamp", String.valueOf(System.currentTimeMillis()))
            .build();
    }

    public boolean isXeroxPrintingEnabled() {
        return ldClient.boolVariation("xerox-printing", ldUser, false);
    }

    public boolean isCtscEnabled() {
        return ldClient.boolVariation("CTSC", ldUser, false);
    }
}
