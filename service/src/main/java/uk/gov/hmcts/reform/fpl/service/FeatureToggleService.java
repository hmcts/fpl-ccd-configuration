package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FeatureToggleService {

    private final LDClient ldClient;
    private final String ldUserKey;
    private final String environment;

    private static final String LOCAL_AUTHORITY_NAME_KEY = "localAuthorityName";

    @Autowired
    public FeatureToggleService(LDClient ldClient, @Value("${ld.user_key}") String ldUserKey,
                                @Value("${fpl.env}") String environment) {
        this.ldClient = ldClient;
        this.ldUserKey = ldUserKey;
        this.environment = environment;
    }

    public boolean isCaseCreationForNotOnboardedUsersEnabled(String localAuthorityCode) {
        return ldClient.boolVariation("allow-case-creation-for-users-not-onboarded-to-mo",
            createLDUser(Map.of(LOCAL_AUTHORITY_NAME_KEY, LDValue.of(localAuthorityCode))), false);
    }

    public boolean isRestrictedFromPrimaryApplicantEmails(String caseId) {
        return ldClient.boolVariation("restrict-primary-applicant-emails",
            createLDUser(Map.of("caseId", LDValue.of(caseId))), false);
    }

    public boolean isRestrictedFromCaseSubmission(String localAuthorityName) {
        return ldClient.boolVariation("restrict-case-submission",
            createLDUser(Map.of(LOCAL_AUTHORITY_NAME_KEY, LDValue.of(localAuthorityName))), false);
    }

    public boolean emailsToSolicitorEnabled(String localAuthorityName) {
        return ldClient.boolVariation("send-la-emails-to-solicitor",
            createLDUser(Map.of(LOCAL_AUTHORITY_NAME_KEY, LDValue.of(localAuthorityName))), true);
    }

    public boolean isSummaryTabFirstCronRunEnabled() {
        return ldClient.boolVariation("summary-tab-first-run", createLDUser(), false);
    }

    public boolean isResendCafcassEmailsEnabled() {
        return ldClient.boolVariation("resend-cafcass-emails-job", createLDUser(), false);
    }

    public boolean isFeeAndPayCaseTypeEnabled() {
        return ldClient.boolVariation("fee-and-pay-case-type", createLDUser(), false);
    }

    public boolean isNewDocumentUploadNotificationEnabled() {
        return ldClient.boolVariation("document-upload-new-notification",
            createLDUser(), false);

    }

    public boolean isFurtherEvidenceDocumentTabEnabled() {
        return ldClient.boolVariation("further-evidence-document-tab",
            createLDUser(), false);
    }

    public boolean isApplicantAdditionalContactsEnabled() {
        return ldClient.boolVariation("applicant-additional-contacts", createLDUser(), false);
    }

    public boolean isLanguageRequirementsEnabled() {
        return ldClient.boolVariation("language-requirements", createLDUser(), false);
    }

    public boolean isCafcassSubjectCategorised() {
        return ldClient.boolVariation("cafcass-subject-category", createLDUser(), false);
    }

    public boolean isSecureDocstoreEnabled() {
        return ldClient.boolVariation("secure-docstore-enabled", createLDUser(), false);
    }

    public String getUserIdsToRemoveRolesFrom() {
        return ldClient.stringVariation("migrate-user-roles", createLDUser(), "");
    }

    public boolean isElinksEnabled() {
        return ldClient.boolVariation("elinks-enabled", createLDUser(), false);
    }

    private LDUser createLDUser() {
        return createLDUser(Map.of());
    }

    private LDUser createLDUser(Map<String, LDValue> values) {
        LDUser.Builder builder = new LDUser.Builder(ldUserKey)
            .custom("timestamp", String.valueOf(System.currentTimeMillis()))
            .custom("environment", environment);

        values.forEach(builder::custom);
        return builder.build();
    }
}
