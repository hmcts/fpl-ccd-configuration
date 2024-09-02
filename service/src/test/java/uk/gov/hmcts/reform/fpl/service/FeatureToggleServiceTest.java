package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.fpl.model.Court;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.matchers.LDUserMatcher.ldUser;

class FeatureToggleServiceTest {

    private static final String LD_USER_KEY = "test_key";
    private static final String ENVIRONMENT = "test_env";
    private static final String LOCAL_AUTHORITY = "test_local_authority";
    private static final String CASE_ID = "1234356";

    private static LDClient ldClient = Mockito.mock(LDClient.class);
    private static FeatureToggleService service = new FeatureToggleService(ldClient, LD_USER_KEY, ENVIRONMENT);
    private static ArgumentCaptor<LDUser> ldUser = ArgumentCaptor.forClass(LDUser.class);

    @AfterEach
    void resetLaunchDarklyClient() {
        reset(ldClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForIsAllowCaseCreationForUsersNotOnboardedToMOEnabled(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isCaseCreationForNotOnboardedUsersEnabled(LOCAL_AUTHORITY)).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("allow-case-creation-for-users-not-onboarded-to-mo"),
            argThat(ldUser(ENVIRONMENT).withLocalAuthority(LOCAL_AUTHORITY).build()),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForIsLocalAuthorityRestrictedFromCaseSubmission(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isRestrictedFromCaseSubmission(LOCAL_AUTHORITY)).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("restrict-case-submission"),
            argThat(ldUser(ENVIRONMENT).withLocalAuthority(LOCAL_AUTHORITY).build()),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForIsSummaryTabFirstCronRunEnabled(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isSummaryTabFirstCronRunEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("summary-tab-first-run"),
            argThat(ldUser(ENVIRONMENT).build()),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForFeeAndPayCaseTypeEnabled(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isFeeAndPayCaseTypeEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("fee-and-pay-case-type"),
            argThat(ldUser(ENVIRONMENT).build()),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForIsNewDocumentUploadNotificationEnabled(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isNewDocumentUploadNotificationEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("document-upload-new-notification"),
            argThat(ldUser(ENVIRONMENT).build()),
            eq(false));
    }

    void shouldMakeCorrectCallForIsLanguageRequirementsEnabled(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isLanguageRequirementsEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("language-requirements"),
            argThat(ldUser(ENVIRONMENT).build()),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForIsFurtherEvidenceDocumentTabEnabled(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isFurtherEvidenceDocumentTabEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("further-evidence-document-tab"),
            argThat(ldUser(ENVIRONMENT).build()),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForIsApplicantAdditionalContactsEnabled(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isApplicantAdditionalContactsEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("applicant-additional-contacts"),
            argThat(ldUser(ENVIRONMENT).build()),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForisCafcassSubjectCategorised(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isCafcassSubjectCategorised()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("cafcass-subject-category"),
            argThat(ldUser(ENVIRONMENT).build()),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123;456"})
    void shouldMakeCorrectCallForGetUserIdsToRemoveRolesFrom(String toggleState) {
        givenToggle(toggleState);

        assertThat(service.getUserIdsToRemoveRolesFrom()).isEqualTo(toggleState);
        verify(ldClient).stringVariation(
            eq("migrate-user-roles"),
            argThat(ldUser(ENVIRONMENT).build()),
            eq(""));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForCourtNotificationEnabledForWa(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isCourtNotificationEnabledForWa(Court.builder().code("151").build()))
            .isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("wa-test-court-notification"),
            argThat(ldUser(ENVIRONMENT).build()),
            eq(true));
    }

    private static List<UserAttribute> buildAttributes(String... additionalAttributes) {
        List<UserAttribute> attributes = new ArrayList<>();

        attributes.add(UserAttribute.forName("timestamp"));
        attributes.add(UserAttribute.forName("environment"));
        Arrays.stream(additionalAttributes)
            .map(UserAttribute::forName)
            .forEach(attributes::add);

        return attributes;
    }

    private void givenToggle(boolean state) {
        when(ldClient.boolVariation(anyString(), any(), anyBoolean())).thenReturn(state);
    }

    private void givenToggle(String state) {
        when(ldClient.stringVariation(anyString(), any(), anyString())).thenReturn(state);
    }
}
