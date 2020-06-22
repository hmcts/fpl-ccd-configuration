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

    private static LDClient ldClient; // Required to be static for use in beforeAll
    private static FeatureToggleService service; //Required to be static for use in beforeAll and argument source

    @Captor
    private ArgumentCaptor<LDUser> ldUser;

    @BeforeAll
    static void beforeAll() {
        // If mocked with the annotation then it is null here and the reference in the service is always null
        // Using post construct doesn't get picked up
        // Doing this in before each is a possibility but that then removes the ability to test and ensure that issue
        //  is removed as we will be creating a new instance of the service before each test so nothing will persist
        ldClient = Mockito.mock(LDClient.class);
        service = new FeatureToggleService(ldClient, LD_USER_KEY);
    }

    @AfterEach
    void tearDown() {
        reset(ldClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForXeroxPrinting(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isXeroxPrintingEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("xerox-printing"), ldUser.capture(), eq(false));

        LDUser expectedLdUser = getLdBuilder().build();

        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(expectedLdUser.getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForCtsc(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isCtscEnabled("test name")).isEqualTo(toggleState);

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

        assertThat(service.isCtscReportEnabled()).isEqualTo(toggleState);

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

        assertThat(service.isFeesEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("FNP"), ldUser.capture(), eq(false));
        LDUser expectedLdUser = getLdBuilder().build();
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(expectedLdUser.getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForPayments(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isPaymentsEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("payments"), ldUser.capture(), eq(false));
        LDUser expectedLdUser = getLdBuilder().build();
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(expectedLdUser.getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForExpertUI(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isExpertUIEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("expert-ui"), ldUser.capture(), eq(false));
        LDUser expectedLdUser = getLdBuilder().build();
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(expectedLdUser.getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForCloseCase(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isCloseCaseEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("close-case"), ldUser.capture(), eq(false));
        LDUser expectedLdUser = getLdBuilder().build();
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(expectedLdUser.getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationCMO(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isAllocatedJudgeNotificationEnabled(CMO)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), ldUser.capture(), eq(false));
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(
            getLdUserWithAllocatedJudgeKey().getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationSDO(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isAllocatedJudgeNotificationEnabled(SDO)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), ldUser.capture(), eq(false));
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(
            getLdUserWithAllocatedJudgeKey().getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationNoticeOfProceedings(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isAllocatedJudgeNotificationEnabled(NOTICE_OF_PROCEEDINGS))
            .isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), ldUser.capture(), eq(false));
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(
            getLdUserWithAllocatedJudgeKey().getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationGeneratedOrder(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isAllocatedJudgeNotificationEnabled(GENERATED_ORDER)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), ldUser.capture(), eq(false));
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(
            getLdUserWithAllocatedJudgeKey().getCustomAttributes());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldMakeCorrectCallForAllocatedJudgeNotificationC2Application(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(service.isAllocatedJudgeNotificationEnabled(C2_APPLICATION)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), ldUser.capture(), eq(false));
        assertThat(ldUser.getValue().getCustomAttributes()).isEqualTo(
            getLdUserWithAllocatedJudgeKey().getCustomAttributes());
    }

    @ParameterizedTest
    @MethodSource("userAttributesTestSource")
    void shouldNotCarryAttributesBetweenRequests(Runnable functionToTest, Runnable accumulateFunction,
                                                 List<UserAttribute> attributes) {
        // dummy code
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
