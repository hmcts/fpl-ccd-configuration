package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.UserAttribute;
import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.CMO;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.GENERATED_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.AllocatedJudgeNotificationType.SDO;

@ExtendWith(SpringExtension.class)
public class FeatureToggleServiceTest {

    private static final String LD_USER_KEY = "test_key";

    @MockBean
    private LDClient ldClient;

    private FeatureToggleService featureToggleService;

    @Captor
    private ArgumentCaptor<LDUser> ldUser;

    @BeforeEach
    void setup() {
        featureToggleService = new FeatureToggleService(ldClient, LD_USER_KEY);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldMakeCorrectCallForXeroxPrinting(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isXeroxPrintingEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("xerox-printing"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldMakeCorrectCallForCtsc(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isCtscEnabled("test name")).isEqualTo(toggleState);
        featureToggleService.isCtscReportEnabled();

        verify(ldClient, atLeast(1)).boolVariation(eq("CTSC"), ldUser.capture(), eq(false));
        assertThat(ldUser.getAllValues().get(0).getCustomAttributes()).doesNotContain(UserAttribute.forName("report"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldMakeCorrectCallForCtscReport(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isCtscReportEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("CTSC"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldMakeCorrectCallForFees(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isFeesEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("FNP"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldMakeCorrectCallForPayments(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isPaymentsEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("payments"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldMakeCorrectCallForExpertUI(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isExpertUIEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("expert-ui"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldMakeCorrectCallForCloseCase(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isCloseCaseEnabled()).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("close-case"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldMakeCorrectCallForAllocatedJudgeNotificationCMO(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isAllocatedJudgeNotificationEnabled(CMO)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldMakeCorrectCallForAllocatedJudgeNotificationSDO(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isAllocatedJudgeNotificationEnabled(SDO)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldMakeCorrectCallForAllocatedJudgeNotificationNoticeOfProceedings(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isAllocatedJudgeNotificationEnabled(NOTICE_OF_PROCEEDINGS))
            .isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldMakeCorrectCallForAllocatedJudgeNotificationGeneratedOrder(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isAllocatedJudgeNotificationEnabled(GENERATED_ORDER)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), any(LDUser.class), eq(false));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldMakeCorrectCallForAllocatedJudgeNotificationC2Application(Boolean toggleState) {
        givenToggle(toggleState);

        assertThat(featureToggleService.isAllocatedJudgeNotificationEnabled(C2_APPLICATION)).isEqualTo(toggleState);

        verify(ldClient).boolVariation(eq("judge-notification"), any(LDUser.class), eq(false));
    }

    private void givenToggle(boolean state) {
        when(ldClient.boolVariation(anyString(), any(), anyBoolean())).thenReturn(state);
    }
}
