package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.client.LDClient;
import com.launchdarkly.client.LDUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class FeatureToggleServiceTest {

    private static final String LD_USER_KEY = "test_key";

    @MockBean
    private LDClient ldClient;

    private FeatureToggleService featureToggleService;

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

        verify(ldClient).boolVariation(eq("CTSC"), any(LDUser.class), eq(false));
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

    private void givenToggle(boolean state) {
        when(ldClient.boolVariation(anyString(), any(), anyBoolean())).thenReturn(state);
    }
}
