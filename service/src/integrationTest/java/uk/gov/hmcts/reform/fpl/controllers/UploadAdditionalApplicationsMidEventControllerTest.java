package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.SupplementsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.Supplements.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

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
        C2DocumentBundle temporaryC2Document = C2DocumentBundle.builder()
            .type(WITH_NOTICE)
            .supplementsBundle(
                List.of(element(SupplementsBundle.builder().name(C13A_SPECIAL_GUARDIANSHIP).build())))
            .build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(AdditionalApplicationType.C2_ORDER))
            .temporaryC2Document(temporaryC2Document)
            .build();

        given(feeService.getFeesDataForAdditionalApplications(
            temporaryC2Document, null, List.of(C13A_SPECIAL_GUARDIANSHIP), List.of()))
            .willReturn(FeesData.builder().totalAmount(BigDecimal.TEN).build());

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData), "get-fee");

        verify(feeService).getFeesDataForAdditionalApplications(
            temporaryC2Document, null, List.of(C13A_SPECIAL_GUARDIANSHIP), List.of());

        assertThat(response.getData())
            .containsEntry("amountToPay", "1000")
            .containsEntry("displayAmountToPay", YES.getValue());
    }

    @Test
    void shouldAddErrorOnFeeRegisterException() {
        given(feeService.getFeesDataForAdditionalApplications(any(), any(), any(), any()))
            .willThrow((new FeeRegisterException(1, "", new Throwable())));

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(AdditionalApplicationType.C2_ORDER))
            .temporaryC2Document(C2DocumentBundle.builder().type(WITH_NOTICE).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData), "get-fee");

        assertThat(response.getData()).containsEntry("displayAmountToPay", NO.getValue());
    }

    @Test
    void shouldDisplayErrorForInvalidPbaNumber() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryPbaPayment", Map.of("pbaNumber", "12345")))
            .build(), "validate");

        assertThat(response.getErrors()).contains("Payment by account (PBA) number must include 7 numbers");
        assertThat(response.getData().get("temporaryPbaPayment"))
            .extracting("pbaNumber").isEqualTo("PBA12345");
    }

    @Test
    void shouldNotDisplayErrorForValidPbaNumber() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryPbaPayment", Map.of("pbaNumber", "1234567")))
            .build(), "validate");

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData().get("temporaryPbaPayment")).extracting("pbaNumber")
            .isEqualTo("PBA1234567");
    }
}
