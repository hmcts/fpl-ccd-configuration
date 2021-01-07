package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.UserAttribute;
import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.matchers.LDUserMatcher.ldUser;

class FeatureToggleServiceTest {

    private static final String LD_USER_KEY = "test_key";
    private static final String ENVIRONMENT = "test_env";
    private static final String LOCAL_AUTHORITY = "test_local_authority";

    private static LDClient ldClient = Mockito.mock(LDClient.class);
    private static FeatureToggleService service = new FeatureToggleService(ldClient, LD_USER_KEY, ENVIRONMENT);
    private static ArgumentCaptor<LDUser> ldUser = ArgumentCaptor.forClass(LDUser.class);

    @AfterEach
    void resetLaunchDarklyClient() {
        reset(ldClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForCtsc(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isCtscEnabled(LOCAL_AUTHORITY)).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("CTSC"),
            ldUser(ENVIRONMENT).withLocalAuthority(LOCAL_AUTHORITY).build(),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForCtscReport(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isCtscReportEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("CTSC"),
            ldUser(ENVIRONMENT).build(),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForCaseUserAssignment(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isCaseUserBulkAssignmentEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("case-user-assignment"),
            ldUser(ENVIRONMENT).build(),
            eq(true));
    }

    @ParameterizedTest
    @MethodSource("userAttributesTestSource")
    void shouldNotAccumulateAttributesBetweenRequests(Runnable functionToTest, Runnable accumulateFunction,
                                                      List<UserAttribute> attributes) {
        accumulateFunction.run();
        functionToTest.run();

        verify(ldClient, times(2)).boolVariation(anyString(), ldUser.capture(), anyBoolean());
        assertThat(ldUser.getValue().getCustomAttributes()).containsExactlyInAnyOrderElementsOf(attributes);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForIsAllowCaseCreationForUsersNotOnboardedToMOEnabled(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isAllowCaseCreationForUsersNotOnboardedToMOEnabled(LOCAL_AUTHORITY)).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("allow-case-creation-for-users-not-onboarded-to-mo"),
            ldUser(ENVIRONMENT).withLocalAuthority(LOCAL_AUTHORITY).build(),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForIsLocalAuthorityRestrictedFromCaseSubmission(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isRestrictedFromCaseSubmission(LOCAL_AUTHORITY)).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("restrict-case-submission"),
            ldUser(ENVIRONMENT).withLocalAuthority(LOCAL_AUTHORITY).build(),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForIsAddHearingsInPastEnabled(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isAddHearingsInPastEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("add-hearings-in-past"),
            ldUser(ENVIRONMENT).build(),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForIsApplicationDocumentsEventEnabled(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isApplicationDocumentsEventEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("application-documents-event"),
            ldUser(ENVIRONMENT).build(),
            eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForIsEpoOrderTypeAndExclusionEnabledBoolean(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isEpoOrderTypeAndExclusionEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(
            eq("epo-order-type-and-exclusion"),
            ldUser(ENVIRONMENT).build(),
            eq(false));
    }

    private static Stream<Arguments> userAttributesTestSource() {
        return Stream.of(
            Arguments.of(
                (Runnable) () -> service.isCtscReportEnabled(),
                (Runnable) () -> service.isCtscEnabled("test name"),
                buildAttributes("report")),
            Arguments.of(
                (Runnable) () -> service.isCtscEnabled("test name"),
                (Runnable) () -> service.isCtscReportEnabled(),
                buildAttributes("localAuthorityName"))
        );
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
}
