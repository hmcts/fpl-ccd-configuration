package uk.gov.hmcts.reform.fpl.controllers;

import com.launchdarkly.client.LDClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadC2DocumentsMidEventControllerTest extends AbstractControllerTest {

    @MockBean
    private LDClient ldClient;

    @MockBean
    private FeeService feeService;

    UploadC2DocumentsMidEventControllerTest() {
        super("upload-c2");
    }

    @Test
    void shouldAddAmountToPayFieldWhenFeatureToggleIsTrue() {
        given(ldClient.boolVariation(eq("FNP"), any(), anyBoolean())).willReturn(true);
        given(feeService.getFeesDataForC2(WITH_NOTICE)).willReturn(FeesData.builder()
            .totalAmount(BigDecimal.TEN)
            .build());

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("c2ApplicationType", Map.of("type", "WITH_NOTICE")))
            .build(), "get-fee");

        verify(feeService).getFeesDataForC2(WITH_NOTICE);
        assertThat(response.getData()).containsEntry("amountToPay", "1000");
    }

    @Test
    void shouldNotAddAmountToPayFieldWhenFeatureToggleIsFalse() {
        given(ldClient.boolVariation(eq("FNP"), any(), anyBoolean())).willReturn(false);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("c2ApplicationType", Map.of("type", "WITH_NOTICE")))
            .build(), "get-fee");

        verify(feeService, never()).getFeesDataForC2(WITH_NOTICE);
        assertThat(response.getData()).doesNotContainKey("amountToPay");
    }

    @Test
    void shouldRemoveTemporaryC2DocumentForEmptyUrl() {
        given(ldClient.boolVariation(eq("FNP"), any(), anyBoolean())).willReturn(false);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("c2Doc", Map.of()))
            .build(), "get-fee");

        assertThat(response.getData()).doesNotContainKey("c2Doc");
    }

    @Test
    void shouldKeepTemporaryC2DocumentForNonEmptyUrl() {
        given(ldClient.boolVariation(eq("FNP"), any(), anyBoolean())).willReturn(false);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryC2Document", Map.of("document", Map.of("url", "example_url"))))
            .build(), "get-fee");

        assertThat(response.getData()).containsKey("temporaryC2Document");
    }

    @Test
    void shouldAddErrorOnFeeRegisterException() {
        given(ldClient.boolVariation(eq("FNP"), any(), anyBoolean())).willReturn(true);
        given(feeService.getFeesDataForC2(any())).willThrow((new FeeRegisterException(1, "", new Throwable())));

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("c2ApplicationType", Map.of("type", "WITH_NOTICE")))
            .build(), "get-fee");

        assertThat(response.getErrors()).contains("XXX");
        assertThat(response.getData()).doesNotContainKeys("amountToPay");
    }
}
