package uk.gov.hmcts.reform.fpl.controllers;

import com.launchdarkly.client.LDClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.service.email.NotificationService;
import uk.gov.hmcts.reform.fpl.service.email.content.FailedPBAPaymentContentProvider;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.NotifyTemplates.*;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadC2DocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadC2DocumentsMidEventControllerTest extends AbstractControllerTest {
    @MockBean
    private LDClient ldClient;

    @MockBean
    private FeeService feeService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private FailedPBAPaymentContentProvider failedPBAPaymentContentProvider;

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
        assertThat(response.getData())
            .containsEntry("amountToPay", "1000")
            .containsEntry("displayAmountToPay", YES.getValue());
    }

    @Test
    void shouldNotAddAmountToPayFieldWhenFeatureToggleIsFalse() {
        given(ldClient.boolVariation(eq("FNP"), any(), anyBoolean())).willReturn(false);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("c2ApplicationType", Map.of("type", "WITH_NOTICE")))
            .build(), "get-fee");

        verify(feeService, never()).getFeesDataForC2(WITH_NOTICE);
        assertThat(response.getData()).doesNotContainKeys("amountToPay", "displayAmountToPay");
    }

    @Test
    void shouldRemoveTemporaryC2DocumentForEmptyUrl() {
        given(ldClient.boolVariation(eq("FNP"), any(), anyBoolean())).willReturn(false);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryC2Document", Map.of("document", Map.of())))
            .build(), "get-fee");

        assertThat(response.getData()).extracting("temporaryC2Document").extracting("document").isNull();
    }

    @Test
    void shouldKeepTemporaryC2DocumentForNonEmptyUrl() {
        given(ldClient.boolVariation(eq("FNP"), any(), anyBoolean())).willReturn(false);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryC2Document", Map.of("document", Map.of("url", "example_url"))))
            .build(), "get-fee");

        assertThat(response.getData()).extracting("temporaryC2Document")
            .extracting("document")
            .extracting("url")
            .isEqualTo("example_url");
    }

    @Test
    void shouldAddErrorOnFeeRegisterException() throws IOException {
        given(ldClient.boolVariation(eq("FNP"), any(), anyBoolean())).willReturn(true);
        given(feeService.getFeesDataForC2(any())).willThrow((new FeeRegisterException(1, "", new Throwable())));

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("c2ApplicationType", Map.of("type", "WITH_NOTICE"),
                "caseLocalAuthority", "example"))
            .id(1L)
            .build(), "get-fee");

        assertThat(response.getData())
            .doesNotContainKey("amountToPay")
            .containsEntry("displayAmountToPay", NO.getValue());
    }

    @Test
    void shouldSendFailedPaymentNotificationOnFeeRegisterException() throws IOException, NotificationClientException {
        CaseDetails details = CaseDetails.builder()
            .data(Map.of(
                "caseLocalAuthority", "example",
                "c2ApplicationType", Map.of("type", "WITH_NOTICE")))
            .id(1L)
            .build();
        given(ldClient.boolVariation(eq("FNP"), any(), anyBoolean())).willReturn(true);
        given(feeService.getFeesDataForC2(any())).willThrow((new FeeRegisterException(1, "", new Throwable())));
        given(failedPBAPaymentContentProvider.buildLANotificationParameters(any())).willReturn(
            Map.of("applicationType", "C2"));

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(details,
            "get-fee");

        verify(notificationService).sendEmail(
            APPLICATION_PBA_PAYMENT_FAILED_TEMPLATE_FOR_LA,
            "local-authority@local-authority.com",
            Map.of("applicationType", "C2"),
            "1");
    }

    private Map<String, Object> getCtscNotificationParametersForFailedPayment() {
        return Map.of("applicationType", "C2",
            "caseUrl", "caseUrl");
    }

    @Test
    void shouldDisplayErrorForInvalidPbaNumber() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryC2Document", Map.of("pbaNumber", "12345")))
            .build(), "validate-pba-number");

        assertThat(response.getErrors()).contains("Payment by account (PBA) number must include 7 numbers");
        assertThat(response.getData()).extracting("temporaryC2Document").extracting("pbaNumber").isEqualTo("PBA12345");
    }

    @Test
    void shouldNotDisplayErrorForValidPbaNumber() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryC2Document", Map.of("pbaNumber", "1234567")))
            .build(), "validate-pba-number");

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData()).extracting("temporaryC2Document")
            .extracting("pbaNumber")
            .isEqualTo("PBA1234567");
    }
}
