package uk.gov.hmcts.reform.fpl.service;

import com.launchdarkly.client.LDClient;
import com.launchdarkly.client.LDUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

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

    @Test
    public void shouldMakeCorrectCallForXeroxPrinting() {
        featureToggleService.isXeroxPrintingEnabled();

        verify(ldClient).boolVariation(eq("xerox-printing"), any(LDUser.class), eq(false));
    }

    @Test
    public void shouldMakeCorrectCallForCtsc() {
        featureToggleService.isCtscEnabled("test name");

        verify(ldClient).boolVariation(eq("CTSC"), any(LDUser.class), eq(false));
    }

    @Test
    public void shouldMakeCorrectCallForFees() {
        featureToggleService.isFeesEnabled();


        verify(ldClient).boolVariation(eq("FNP"), any(LDUser.class), eq(false));
    }


    @Test
    public void shouldMakeCorrectCallForPayments() {
        featureToggleService.isPaymentsEnabled();

        verify(ldClient).boolVariation(eq("payments"), any(LDUser.class), eq(false));
    }
}
