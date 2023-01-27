package uk.gov.hmcts.reform.fpl.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.model.FailedPayment;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class ReviewFailedPaymentControllerAboutToStartTest extends AbstractCallbackTest {

    ReviewFailedPaymentControllerAboutToStartTest() {
        super("review-failed-payment");
    }

    @Test
    void aboutToStartShouldPrePopulateHasToBeReviewedFailedPayments() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("failedPayments", List.of()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);

        assertThat(callbackResponse.getData()).containsKey("hasToBeReviewedFailedPayments")
            .containsEntry("hasToBeReviewedFailedPayments", "NO");
    }

    @Test
    void aboutToStartShouldPrePopulateListOfToBeReviewedFailedPayments() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("failedPayments", List.of(
                ElementUtils.element(UUID.fromString("11111111-1111-1111-1111-111111111111"),
                FailedPayment.builder()
                    .orderApplicantName("<orderApplicantName>")
                    .orderApplicantType("<orderApplicantType>")
                    .applicationTypes(List.of(ApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN))
                    .paymentAt("15 October 2020 4pm")
                    .build())
            )))
            .build();
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToStartEvent(caseDetails);
        Map<String, Object>  examine = callbackResponse.getData();

        Map<String, Object> map = new HashMap<>();
        map.put("value", null);
        map.put("list_items", List.of(Map.of("code", "11111111-1111-1111-1111-111111111111",
            "label", "15 October 2020 4pm: C1 - Appointment of a guardian by <orderApplicantName>")));

        assertThat(callbackResponse.getData()).containsKeys("hasToBeReviewedFailedPayments",
                "listOfToBeReviewedFailedPayments")
            .containsEntry("hasToBeReviewedFailedPayments", "YES")
            .containsEntry("listOfToBeReviewedFailedPayments", map);
        System.out.println();
    }

}
