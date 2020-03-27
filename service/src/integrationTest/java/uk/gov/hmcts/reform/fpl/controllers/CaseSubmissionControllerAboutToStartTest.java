package uk.gov.hmcts.reform.fpl.controllers;

import com.launchdarkly.client.LDClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerAboutToStartTest extends AbstractControllerTest {

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private FeeService feeService;

    @MockBean
    private LDClient ldClient;

    CaseSubmissionControllerAboutToStartTest() {
        super("case-submission");
    }

    @BeforeEach
    void mocking() {
        given(userDetailsService.getUserName(userAuthToken)).willReturn("Emma Taylor");
    }

    @Test
    void shouldAddConsentLabelToCaseDetails() {
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(CaseDetails.builder()
            .data(Map.of("caseName", "title"))
            .build());

        assertThat(callbackResponse.getData())
            .containsEntry("caseName", "title")
            .containsEntry("submissionConsentLabel",
                "I, Emma Taylor, believe that the facts stated in this application are true.");
    }

    @Test
    void shouldAddAmountToPayFieldWhenFeatureToggleIsTrue() {
        Orders orders = Orders.builder().orderType(List.of(OrderType.CARE_ORDER)).build();
        FeesData feesData = FeesData.builder()
            .totalAmount(BigDecimal.valueOf(123))
            .build();

        givenPaymentToggle(true);
        given(feeService.getFeesDataForOrders(orders)).willReturn(feesData);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(CaseDetails.builder()
            .data(Map.of("orders", orders))
            .build());

        assertThat(response.getData()).containsEntry("amountToPay", "12300");
        assertThat(response.getData()).containsEntry("displayAmountToPay", YES.getValue());
    }

    @Test
    void shouldNotAddAmountToPayFieldWhenFeatureToggleIsFalse() {
        givenPaymentToggle(false);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(CaseDetails.builder()
            .data(Map.of())
            .build());

        verify(feeService, never()).getFeesDataForOrders(any());
        assertThat(response.getData()).doesNotContainKeys("amountToPay", "displayAmountToPay");
    }

    @Test
    void shouldNotDisplayAmountToPayFieldWhenErrorIsThrown() {
        givenPaymentToggle(true);
        given(feeService.getFeesDataForOrders(any())).willThrow(new FeeRegisterException(300, "duplicate", null));

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(CaseDetails.builder()
            .data(Map.of())
            .build());

        assertThat(response.getData()).doesNotContainKey("amountToPay");
        assertThat(response.getData()).containsEntry("displayAmountToPay", NO.getValue());
    }

    private void givenPaymentToggle(boolean enabled) {
        given(ldClient.boolVariation(eq("FNP"), any(), anyBoolean())).willReturn(enabled);
    }

    @Nested
    class LocalAuthorityValidation {

        @Test
        void shouldReturnErrorWhenCaseBelongsToSmokeTestLocalAuthority() {
            CaseDetails caseDetails = prepareCaseBelongingTo("FPLA");
            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

            assertThat(callbackResponse.getData()).containsEntry("caseLocalAuthority", "FPLA");
            assertThat(callbackResponse.getErrors()).contains("Test local authority cannot submit cases");
        }

        @Test
        void shouldReturnNoErrorWhenCaseBelongsToRegularLocalAuthority() {
            CaseDetails caseDetails = prepareCaseBelongingTo("SA");
            AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

            assertThat(callbackResponse.getData()).containsEntry("caseLocalAuthority", "SA");
            assertThat(callbackResponse.getErrors()).isEmpty();
        }

        private CaseDetails prepareCaseBelongingTo(String localAuthority) {
            return CaseDetails.builder()
                .data(Map.of("caseLocalAuthority", localAuthority))
                .build();
        }
    }
}
