package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType;

import java.util.Map;

@Service
public class FeatureToggleService {

    private final LDClient ldClient;
    private final String ldUserKey;

    @Autowired
    public FeatureToggleService(LDClient ldClient, @Value("${ld.user_key}") String ldUserKey) {
        this.ldClient = ldClient;
        this.ldUserKey = ldUserKey;
    }

    public boolean isXeroxPrintingEnabled() {
        return ldClient.boolVariation("xerox-printing", createLDUser(), false);
    }

    public boolean isCtscEnabled(String localAuthorityName) {
        return ldClient.boolVariation("CTSC",
            createLDUser(Map.of("localAuthorityName", LDValue.of(localAuthorityName))),false);
    }

    public boolean isCtscReportEnabled() {
        return ldClient.boolVariation("CTSC",
            createLDUser(Map.of("localAuthorityName", LDValue.of(true))),false);
    }

    public boolean isAllocatedJudgeNotificationEnabled(AllocatedJudgeNotificationType allocatedJudgeNotificationType) {
        LDUser launchDarklyUser = createLDUser(Map.of("allocatedJudgeNotificationType",
            LDValue.of(allocatedJudgeNotificationType.getValue())));

        return ldClient.boolVariation("judge-notification", launchDarklyUser,false);
    }

    public boolean isFeesEnabled() {
        return ldClient.boolVariation("FNP", createLDUser(), false);
    }

    //TODO: use FNP flag once PaymentsApi is deployed to AAT
    public boolean isPaymentsEnabled() {
        return ldClient.boolVariation("payments", createLDUser(), false);
    }

    public boolean isExpertUIEnabled() {
        return ldClient.boolVariation("expert-ui", createLDUser(), false);
    }

    public boolean isCloseCaseEnabled() {
        return ldClient.boolVariation("close-case", createLDUser(), false);
    }

    private LDUser createLDUser() {
        return createLDUser(Map.of());
    }

    private LDUser createLDUser(Map<String, LDValue> values) {
        LDUser.Builder builder = new LDUser.Builder(ldUserKey)
            .custom("timestamp", String.valueOf(System.currentTimeMillis()));

        values.forEach(builder::custom);
        return builder.build();
    }
}
