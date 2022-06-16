package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.orders.validator.OrdersNeededValidator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(OrdersNeededController.class)
@OverrideAutoConfiguration(enabled = true)
class OrdersNeededMidEventCallbackControllerTest extends AbstractCallbackTest {

    @MockBean
    private OrdersNeededValidator ordersNeededValidator;

    OrdersNeededMidEventCallbackControllerTest() {
        super("orders-needed");
    }

    @Test
    void showReturnErrorWhenErrorOccurs() {
        List<String> errors = List.of("error1", "error2");
        when(ordersNeededValidator.validate(any())).thenReturn(errors);
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(CaseData.builder().build());
        assertThat(callbackResponse.getErrors()).isEqualTo(errors);
    }
}
