package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.UserAttribute;
import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.SDO;

@ExtendWith(MockitoExtension.class)
class FeatureToggleServiceTest {

    private static final String LD_USER_KEY = "test_key";

    private static LDClient ldClient;
    private static FeatureToggleService featureToggleService;

    @Captor
    private ArgumentCaptor<LDUser> ldUser;

    @BeforeAll
    static void beforeAll() {
        ldClient = Mockito.mock(LDClient.class); // mock to be seen by static field
        featureToggleService = new FeatureToggleService(ldClient, LD_USER_KEY);
    }

    @AfterEach
    void tearDown() {
        reset(ldClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForXeroxPrinting(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isXeroxPrintingEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("xerox-printing"), ldUser.capture(), eq(false));

        LDUser expectedLdUser = getLdBuilder().build();

        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(expectedLdUser.getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForCtsc(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isCtscEnabled("test name")).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("CTSC"), ldUser.capture(), eq(false));

        LDUser expectedLdUser = getLdBuilder()
            .custom("localAuthorityName", "")
            .build();

        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(expectedLdUser.getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForCtscReport(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isCtscReportEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("CTSC"), ldUser.capture(), eq(false));

        LDUser expectedLdUser = getLdBuilder()
            .custom("report", "")
            .build();

        assertThat(ldUser.getAllValues().get(0).getCustomAttributes()).isEqualTo(expectedLdUser.getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForFees(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isFeesEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("FNP"), ldUser.capture(), eq(false));
        LDUser expectedLdUser = getLdBuilder().build();
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(expectedLdUser.getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForPayments(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isPaymentsEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("payments"), ldUser.capture(), eq(false));
        LDUser expectedLdUser = getLdBuilder().build();
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(expectedLdUser.getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForExpertUI(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isExpertUIEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("expert-ui"), ldUser.capture(), eq(false));
        LDUser expectedLdUser = getLdBuilder().build();
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(expectedLdUser.getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForCloseCase(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isCloseCaseEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("close-case"), ldUser.capture(), eq(false));
        LDUser expectedLdUser = getLdBuilder().build();
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(expectedLdUser.getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationCMO(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isAllocatedJudgeNotificationEnabled(CMO)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), ldUser.capture(), eq(false));
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(
            getLdUserWithAllocatedJudgeKey().getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationSDO(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isAllocatedJudgeNotificationEnabled(SDO)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), ldUser.capture(), eq(false));
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(
            getLdUserWithAllocatedJudgeKey().getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationNoticeOfProceedings(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isAllocatedJudgeNotificationEnabled(NOTICE_OF_PROCEEDINGS))
            .isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), ldUser.capture(), eq(false));
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(
            getLdUserWithAllocatedJudgeKey().getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationGeneratedOrder(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isAllocatedJudgeNotificationEnabled(GENERATED_ORDER)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), ldUser.capture(), eq(false));
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(
            getLdUserWithAllocatedJudgeKey().getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationC2Application(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isAllocatedJudgeNotificationEnabled(C2_APPLICATION)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), ldUser.capture(), eq(false));
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(
            getLdUserWithAllocatedJudgeKey().getCustomAttributes());
    }

    @ParameterizedTest
    @MethodSource("featureToggleFunctions")
    void shouldNotCarryAttributesBetweenRequests(Runnable function) {
        // dummy code
        LDUser dummyUser = new LDUser.Builder("dummyKey").custom("dummyAttribute", "dummyValue").build();
        ldClient.boolVariation("dummy", dummyUser, false);

        function.run();

        verify(ldClient, times(2)).boolVariation(anyString(), ldUser.capture(), anyBoolean());
        
        assertThat(ldUser.getValue().getCustomAttributes()).doesNotContain(UserAttribute.forName("dummyAttribute"));
    }

    private static Stream<Arguments> featureToggleFunctions() {
        return Stream.of(
            Arguments.of((Runnable) () -> featureToggleService.isCloseCaseEnabled()),
            Arguments.of((Runnable) () -> featureToggleService.isCtscReportEnabled()),
            Arguments.of((Runnable) () -> featureToggleService.isAllocatedJudgeNotificationEnabled(SDO)),
            Arguments.of((Runnable) () -> featureToggleService.isCtscEnabled("test name")),
            Arguments.of((Runnable) () -> featureToggleService.isExpertUIEnabled()),
            Arguments.of((Runnable) () -> featureToggleService.isFeesEnabled()),
            Arguments.of((Runnable) () -> featureToggleService.isPaymentsEnabled()),
            Arguments.of((Runnable) () -> featureToggleService.isXeroxPrintingEnabled())
        );
    }

    private void givenToggle(boolean state) {
        when(ldClient.boolVariation(anyString(), any(), anyBoolean())).thenReturn(state);
    }

    private LDUser.Builder getLdBuilder() {
        return new LDUser.Builder(LD_USER_KEY)
            .custom("timestamp", "");
    }

    private LDUser getLdUserWithAllocatedJudgeKey() {
        return getLdBuilder()
            .custom("allocatedJudgeNotificationType", "")
            .build();
    }
}
