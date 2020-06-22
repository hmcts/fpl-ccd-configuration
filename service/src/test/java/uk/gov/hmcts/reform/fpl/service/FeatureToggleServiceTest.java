package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.UserAttribute;
import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.SDO;

@ExtendWith(MockitoExtension.class)
class FeatureToggleServiceTest {

    private static final String LD_USER_KEY = "test_key";

    private static LDClient ldClient = Mockito.mock(LDClient.class);
    private static FeatureToggleService service = new FeatureToggleService(ldClient, LD_USER_KEY);

    @Captor
    private ArgumentCaptor<LDUser> ldUser;

    @AfterEach
    void resetLaunchDarklyClient() {
        reset(ldClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForCtsc(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isCtscEnabled("test name")).isEqualTo(toggleState);
        verify(ldClient).boolVariation(eq("CTSC"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForCtscReport(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isCtscReportEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(eq("CTSC"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForExpertUI(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isExpertUIEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(eq("expert-ui"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForCloseCase(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isCloseCaseEnabled()).isEqualTo(toggleState);
        verify(ldClient).boolVariation(eq("close-case"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationCMO(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isAllocatedJudgeNotificationEnabled(CMO)).isEqualTo(toggleState);
        verify(ldClient).boolVariation(eq("judge-notification"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationSDO(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isAllocatedJudgeNotificationEnabled(SDO)).isEqualTo(toggleState);
        verify(ldClient).boolVariation(eq("judge-notification"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationNoticeOfProceedings(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isAllocatedJudgeNotificationEnabled(NOTICE_OF_PROCEEDINGS))
            .isEqualTo(toggleState);
        verify(ldClient).boolVariation(eq("judge-notification"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationGeneratedOrder(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isAllocatedJudgeNotificationEnabled(GENERATED_ORDER)).isEqualTo(toggleState);
        verify(ldClient).boolVariation(eq("judge-notification"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationC2Application(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isAllocatedJudgeNotificationEnabled(C2_APPLICATION)).isEqualTo(toggleState);
        verify(ldClient).boolVariation(eq("judge-notification"), any(LDUser.class), eq(false));
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

    private static Stream<Arguments> userAttributesTestSource() {
        return Stream.of(
            Arguments.of(
                (Runnable) () -> service.isCloseCaseEnabled(),
                (Runnable) () -> service.isCtscReportEnabled(),
                buildAttributes()),
            Arguments.of(
                (Runnable) () -> service.isCtscReportEnabled(),
                (Runnable) () -> service.isCtscEnabled("test name"),
                buildAttributes("report")),
            Arguments.of(
                (Runnable) () -> service.isAllocatedJudgeNotificationEnabled(SDO),
                (Runnable) () -> service.isCtscReportEnabled(),
                buildAttributes("allocatedJudgeNotificationType")),
            Arguments.of(
                (Runnable) () -> service.isCtscEnabled("test name"),
                (Runnable) () -> service.isCtscReportEnabled(),
                buildAttributes("localAuthorityName")),
            Arguments.of(
                (Runnable) () -> service.isExpertUIEnabled(),
                (Runnable) () -> service.isCtscReportEnabled(),
                buildAttributes()),
            Arguments.of(
                (Runnable) () -> service.isFeesEnabled(),
                (Runnable) () -> service.isCtscReportEnabled(),
                buildAttributes()),
            Arguments.of(
                (Runnable) () -> service.isPaymentsEnabled(),
                (Runnable) () -> service.isCtscReportEnabled(),
                buildAttributes()),
            Arguments.of(
                (Runnable) () -> service.isXeroxPrintingEnabled(),
                (Runnable) () -> service.isCtscReportEnabled(),
                buildAttributes())
        );
    }

    private static List<UserAttribute> buildAttributes(String... additionalAttributes) {
        List<UserAttribute> attributes = new ArrayList<>();

        attributes.add(UserAttribute.forName("timestamp"));
        Arrays.stream(additionalAttributes)
            .map(UserAttribute::forName)
            .forEach(attributes::add);

        return attributes;
    }

    private void givenToggle(boolean state) {
        when(ldClient.boolVariation(anyString(), any(), anyBoolean())).thenReturn(state);
    }
}
