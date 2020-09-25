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

    public boolean isTaskListInProgressTagsEnabled() {
        return ldClient.boolVariation("task-list-in-progress-tags", createLDUser(), false);
    }

    public boolean isCtscEnabled(String localAuthorityName) {
        return ldClient.boolVariation("CTSC",
            createLDUser(Map.of("localAuthorityName", LDValue.of(localAuthorityName))),false);
    }

    public boolean isCtscReportEnabled() {
        return ldClient.boolVariation("CTSC",
            createLDUser(Map.of("report", LDValue.of(true))),false);
    }

    public boolean isAllocatedJudgeNotificationEnabled(AllocatedJudgeNotificationType allocatedJudgeNotificationType) {
        LDUser launchDarklyUser = createLDUser(Map.of("allocatedJudgeNotificationType",
            LDValue.of(allocatedJudgeNotificationType.getValue())));

        return ldClient.boolVariation("judge-notification", launchDarklyUser,false);
    }

    public boolean isExpertUIEnabled() {
        return ldClient.boolVariation("expert-ui", createLDUser(), false);
    }

    public boolean isCloseCaseEnabled() {
        return ldClient.boolVariation("close-case", createLDUser(), false);
    }

    public boolean isNewCaseStateModelEnabled() {
        return ldClient.boolVariation("new-case-state-model", createLDUser(), false);
    }

    public boolean isAllowCaseCreationForUsersNotOnboardedToMOEnabled(String localAuthorityName) {
        return ldClient.boolVariation("allow-case-creation-for-users-not-onboarded-to-mo",
            createLDUser(Map.of("localAuthorityName", LDValue.of(localAuthorityName))), false);
    }

    public boolean isRestrictedFromCaseSubmission(String localAuthorityName) {
        return ldClient.boolVariation("restrict-case-submission",
            createLDUser(Map.of("localAuthorityName", LDValue.of(localAuthorityName))), false);
    }

    public boolean isSendLAEmailsToSolicitorEnabled(String localAuthorityName) {
        return ldClient.boolVariation("send-la-emails-to-solicitor",
            createLDUser(Map.of("localAuthorityName", LDValue.of(localAuthorityName))), false);
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
