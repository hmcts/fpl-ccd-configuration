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
    private final LDUser.Builder ldUserBuilder;

    @Autowired
    public FeatureToggleService(LDClient ldClient, @Value("${ld.user_key}") String ldUserKey) {
        this.ldClient = ldClient;
        this.ldUserBuilder = new LDUser.Builder(ldUserKey)
            .custom("timestamp", String.valueOf(System.currentTimeMillis()));
        this.ldUser = this.ldUserBuilder.build();

    }

    public boolean isXeroxPrintingEnabled() {
        return ldClient.boolVariation("xerox-printing", ldUser, false);
    }

    public boolean isCtscEnabled(String localAuthorityName) {
        return ldClient.boolVariation("CTSC",
            ldUserBuilder.custom("localAuthorityName", localAuthorityName).build(),
            false);
    }

    public boolean isCtscReportEnabled() {
        return ldClient.boolVariation("CTSC",
            ldUserBuilder.custom("report", true).build(),
            false);
    }

    public boolean isFeesEnabled() {
        return ldClient.boolVariation("FNP", ldUser, false);
    }

    //TODO: use FNP flag once PaymentsApi is deployed to AAT
    public boolean isPaymentsEnabled() {
        return ldClient.boolVariation("payments", ldUser, false);
    }
}
