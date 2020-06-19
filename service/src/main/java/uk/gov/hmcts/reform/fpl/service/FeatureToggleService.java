package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType;

@Service
public class FeatureToggleService {

    private final LDClient ldClient;
    private final LDUser ldUser;
    private final LDUser.Builder ldUserBuilder;
    private final String ldUserKey;

    @Autowired
    public FeatureToggleService(LDClient ldClient, @Value("${ld.user_key}") String ldUserKey) {
        this.ldClient = ldClient;
        this.ldUserBuilder = new LDUser.Builder(ldUserKey)
            .custom("timestamp", String.valueOf(System.currentTimeMillis()));
        this.ldUser = this.ldUserBuilder.build();
        this.ldUserKey = ldUserKey;
    }

    public boolean isXeroxPrintingEnabled() {
        return ldClient.boolVariation("xerox-printing", ldUser, false);
    }

    public boolean isCtscEnabled(String localAuthorityName) {
        return ldClient.boolVariation("CTSC",
            newUser("localAuthorityName", localAuthorityName),false);
    }

    public boolean isCtscReportEnabled() {
        return ldClient.boolVariation("CTSC",
            ldUserBuilder.custom("report", true).build(),
            false);
    }

    public boolean isAllocatedJudgeNotificationEnabled(AllocatedJudgeNotificationType allocatedJudgeNotificationType) {
        return ldClient.boolVariation("judge-notification",
            ldUserBuilder.custom("allocatedJudgeNotificationType", allocatedJudgeNotificationType.getValue()).build(),
            false);
    }

    public boolean isFeesEnabled() {
        return ldClient.boolVariation("FNP", ldUser, false);
    }

    //TODO: use FNP flag once PaymentsApi is deployed to AAT
    public boolean isPaymentsEnabled() {
        return ldClient.boolVariation("payments", ldUser, false);
    }

    public boolean isExpertUIEnabled() {
        return ldClient.boolVariation("expert-ui", ldUser, false);
    }

    public boolean isCloseCaseEnabled() {
        return ldClient.boolVariation("close-case", ldUser, false);
    }

    private LDUser newUser(String propertyName, String propertyValue) {
        return new LDUser.Builder(ldUserKey)
            .custom("timestamp", String.valueOf(System.currentTimeMillis())).custom(propertyName, propertyValue)
            .build();
    }
}
