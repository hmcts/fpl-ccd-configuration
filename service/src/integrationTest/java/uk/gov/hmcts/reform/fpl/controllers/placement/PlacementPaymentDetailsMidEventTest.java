package uk.gov.hmcts.reform.fpl.controllers.placement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.PlacementController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(PlacementController.class)
@OverrideAutoConfiguration(enabled = true)
class PlacementPaymentDetailsMidEventTest extends AbstractPlacementControllerTest {

    @Test
    void shouldReturnErrorWhenPBANumberIsInvalid() {

        final PBAPayment paymentDetails = PBAPayment.builder()
            .pbaNumber("123")
            .build();

        final CaseData caseData = CaseData.builder()
            .placementEventData(PlacementEventData.builder()
                .placementPayment(paymentDetails)
                .build())
            .build();

        final AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "payment-details");

        assertThat(response.getErrors())
            .containsExactly("Payment by account (PBA) number must include 7 numbers and the PBA prefix");
    }

    @Test
    void shouldNormaliseValidPBANumber() {

        final PBAPayment paymentDetails = PBAPayment.builder()
            .pbaNumber("1234567")
            .build();

        final CaseData caseData = CaseData.builder()
            .placementEventData(PlacementEventData.builder()
                .placementPayment(paymentDetails)
                .build())
            .build();

        final CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "payment-details"));

        final PlacementEventData actualPlacementData = updatedCaseData.getPlacementEventData();

        final PBAPayment expectedPaymentDetails = PBAPayment.builder()
            .pbaNumber("PBA1234567")
            .build();

        assertThat(actualPlacementData.getPlacementPayment()).isEqualTo(expectedPaymentDetails);
    }
}
