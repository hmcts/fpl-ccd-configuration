package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.model.fee.FeeType;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.C2_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.OTHER_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C20_SECURE_ACCOMMODATION;
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
    void shouldCalculateFeeForSelectedOrderBundlesAndAddAmountToPayField() {
        C2DocumentBundle temporaryC2Document = C2DocumentBundle.builder()
            .type(WITH_NOTICE)
            .supplementsBundle(
                List.of(element(Supplement.builder().name(C13A_SPECIAL_GUARDIANSHIP).build())))
            .build();

        OtherApplicationsBundle temporaryOtherDocument = OtherApplicationsBundle.builder()
            .applicationType(OtherApplicationType.C1_PARENTAL_RESPONSIBILITY)
            .parentalResponsibilityType(ParentalResponsibilityType.PR_BY_FATHER)
            .document(DocumentReference.builder().build())
            .supplementsBundle(
                List.of(element(Supplement.builder().name(C20_SECURE_ACCOMMODATION)
                    .secureAccommodationType(SecureAccommodationType.WALES).build())))
            .build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(C2_ORDER, OTHER_ORDER))
            .temporaryOtherApplicationsBundle(temporaryOtherDocument)
            .temporaryC2Document(temporaryC2Document)
            .build();

        List<FeeType> feeTypes = List.of(FeeType.C2_WITH_NOTICE, FeeType.SPECIAL_GUARDIANSHIP,
            FeeType.PARENTAL_RESPONSIBILITY_FATHER, FeeType.SECURE_ACCOMMODATION_WALES);

        given(feeService.getFeesDataForAdditionalApplications(feeTypes))
            .willReturn(FeesData.builder().totalAmount(BigDecimal.TEN).build());

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData), "get-fee");

        verify(feeService).getFeesDataForAdditionalApplications(feeTypes);
        assertThat(response.getData())
            .containsEntry("amountToPay", "1000")
            .containsEntry("displayAmountToPay", YES.getValue());
    }

    @Test
    void shouldAddErrorOnFeeRegisterException() {
        given(feeService.getFeesDataForAdditionalApplications(any()))
            .willThrow((new FeeRegisterException(1, "", new Throwable())));

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(C2_ORDER))
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

    @Test
    void shouldNotValidatePbaNumberWhenPBAPaymentIsNull() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            CaseDetails.builder().data(Collections.emptyMap()).build(), "validate");

        assertThat(response.getErrors()).isEmpty();
    }
}
