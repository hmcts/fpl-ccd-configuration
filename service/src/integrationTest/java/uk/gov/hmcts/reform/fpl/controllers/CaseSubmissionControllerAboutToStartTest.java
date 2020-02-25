package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("integration-test")
@WebMvcTest(CaseSubmissionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseSubmissionControllerAboutToStartTest extends AbstractControllerTest {

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private FeeService feeService;

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
    void shouldAddAmountToPayField() {
        Orders orders = Orders.builder().orderType(List.of(OrderType.CARE_ORDER)).build();

        given(feeService.getFeeAmountForOrders(eq(orders))).willReturn(BigDecimal.valueOf(123));

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(CaseDetails.builder()
            .data(Map.of("orders", orders))
            .build());

        assertThat(response.getData()).containsEntry("amountToPay", "12300");
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
