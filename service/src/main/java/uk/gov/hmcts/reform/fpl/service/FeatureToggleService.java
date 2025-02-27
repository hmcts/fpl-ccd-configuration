package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.sdk.ContextBuilder;
import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiFeatureFlag;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
public class FeatureToggleService {

    private final LDClient ldClient;
    private final String ldUserKey;
    private final String environment;

    private static final String LOCAL_AUTHORITY_NAME_KEY = "localAuthorityName";
    private static final String COURT_CODE_KEY = "courtCode";

    @Autowired
    public FeatureToggleService(LDClient ldClient, @Value("${ld.user_key}") String ldUserKey,
                                @Value("${fpl.env}") String environment) {
        this.ldClient = ldClient;
        this.ldUserKey = ldUserKey;
        this.environment = environment;
    }

    public boolean isCaseCreationForNotOnboardedUsersEnabled(String localAuthorityCode) {
        return ldClient.boolVariation("allow-case-creation-for-users-not-onboarded-to-mo",
            createLDContext(Map.of(LOCAL_AUTHORITY_NAME_KEY, LDValue.of(localAuthorityCode))), false);
    }

    public boolean isRestrictedFromPrimaryApplicantEmails(String caseId) {
        return ldClient.boolVariation("restrict-primary-applicant-emails",
            createLDContext(Map.of("caseId", LDValue.of(caseId))), false);
    }

    public boolean isRestrictedFromCaseSubmission(String localAuthorityName) {
        return ldClient.boolVariation("restrict-case-submission",
            createLDContext(Map.of(LOCAL_AUTHORITY_NAME_KEY, LDValue.of(localAuthorityName))), false);
    }

    public boolean emailsToSolicitorEnabled(String localAuthorityName) {
        return ldClient.boolVariation("send-la-emails-to-solicitor",
            createLDContext(Map.of(LOCAL_AUTHORITY_NAME_KEY, LDValue.of(localAuthorityName))), true);
    }

    public boolean isSummaryTabFirstCronRunEnabled() {
        return ldClient.boolVariation("summary-tab-first-run", createLDContext(), false);
    }

    public boolean isResendCafcassEmailsEnabled() {
        return ldClient.boolVariation("resend-cafcass-emails-job", createLDContext(), false);
    }

    public boolean isFeeAndPayCaseTypeEnabled() {
        return ldClient.boolVariation("fee-and-pay-case-type", createLDContext(), false);
    }

    public boolean isNewDocumentUploadNotificationEnabled() {
        return ldClient.boolVariation("document-upload-new-notification",
            createLDContext(), false);

    }

    public boolean isFurtherEvidenceDocumentTabEnabled() {
        return ldClient.boolVariation("further-evidence-document-tab",
            createLDContext(), false);
    }

    public boolean isApplicantAdditionalContactsEnabled() {
        return ldClient.boolVariation("applicant-additional-contacts", createLDContext(), false);
    }

    public boolean isLanguageRequirementsEnabled() {
        return ldClient.boolVariation("language-requirements", createLDContext(), false);
    }

    public boolean isCafcassSubjectCategorised() {
        return ldClient.boolVariation("cafcass-subject-category", createLDContext(), false);
    }

    public boolean isSecureDocstoreEnabled() {
        return ldClient.boolVariation("secure-docstore-enabled", createLDContext(), false);
    }

    public String getUserIdsToRemoveRolesFrom() {
        return ldClient.stringVariation("migrate-user-roles", createLDContext(), "");
    }

    public boolean isElinksEnabled() {
        return ldClient.boolVariation("elinks-enabled", createLDContext(), false);
    }

    public boolean isCourtNotificationEnabledForWa(Court court) {
        return ldClient.boolVariation("wa-test-court-notification",
            createLDContext(Map.of(COURT_CODE_KEY, LDValue.of(court.getCode()))), true);
    }

    public boolean isCafcassAPIEnabledForCourt(Court court) {
        CafcassApiFeatureFlag flag = getCafcassAPIFlag();

        if (flag.isEnableApi()) {
            if (isEmpty(flag.getWhitelist())) {
                return true;
            } else {
                return flag.getWhitelist().stream()
                    .anyMatch(whiteListCode -> court.getCode().equalsIgnoreCase(whiteListCode));
            }
        }
        return false;
    }

    public CafcassApiFeatureFlag getCafcassAPIFlag() {
        LDValue flag = ldClient.jsonValueVariation("cafcass-api-court", createLDContext(), LDValue.ofNull());

        LDValue whiteList = flag.get("whitelist");
        return CafcassApiFeatureFlag.builder()
            .enableApi(flag.get("enableApi").booleanValue())
            .whitelist((!whiteList.isNull())
                ? StreamSupport.stream(whiteList.valuesAs(LDValue.Convert.String).spliterator(), false)
                    .collect(Collectors.toList())
                : null)
            .build();
    }

    public boolean isHideJobTitleInCaseSubmissionFormEnabled() {
        return ldClient.boolVariation("hide-job-title-in-case-submission-form", createLDContext(), false);
    }

    private LDContext createLDContext() {
        return createLDContext(Map.of());
    }

    private LDContext createLDContext(Map<String, LDValue> values) {
        ContextBuilder builder = LDContext.builder(ldUserKey)
            .set("timestamp", String.valueOf(System.currentTimeMillis()))
            .set("environment", environment);

        values.forEach(builder::set);
        return builder.build();
    }
}
