package uk.gov.hmcts.reform.fpl.controllers;

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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadAdditionalApplicationsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadAdditionalApplicationsMidEventControllerTest extends AbstractCallbackTest {

    @MockBean
    private FeeService feeService;

    UploadAdditionalApplicationsMidEventControllerTest() {
        super("upload-additional-applications");
    }

    @Test
    void shouldAddAmountToPayField() {
        given(feeService.getFeesDataForC2(WITH_NOTICE)).willReturn(FeesData.builder()
            .totalAmount(BigDecimal.TEN)
            .build());

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("c2Type","WITH_NOTICE"))
            .build(), "get-fee");

        verify(feeService).getFeesDataForC2(WITH_NOTICE);
        assertThat(response.getData())
            .containsEntry("amountToPay", "1000")
            .containsEntry("displayAmountToPay", YES.getValue());
    }

    @Test
    void shouldAddErrorOnFeeRegisterException() {
        given(feeService.getFeesDataForC2(any())).willThrow((new FeeRegisterException(1, "", new Throwable())));

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("c2ApplicationType", Map.of("type", "WITH_NOTICE")))
            .build(), "get-fee");

        assertThat(response.getData())
            .doesNotContainKey("amountToPay")
            .containsEntry("displayAmountToPay", NO.getValue());
    }

    @Test
    void shouldDisplayErrorForInvalidPbaNumber() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("pbaNumber", "12345"))
            .build(), "validate");

        assertThat(response.getErrors()).contains("Payment by account (PBA) number must include 7 numbers");
        assertThat(response.getData()).extracting("pbaNumber").isEqualTo("PBA12345");
    }

    @Test
    void shouldNotDisplayErrorForValidPbaNumber() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("pbaNumber", "1234567")).build(), "validate");

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData()).extracting("pbaNumber")
            .isEqualTo("PBA1234567");
    }
}
