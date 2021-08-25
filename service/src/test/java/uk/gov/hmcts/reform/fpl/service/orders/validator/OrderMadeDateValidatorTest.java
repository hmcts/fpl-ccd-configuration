package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.ORDER_PLACED_CHILD_IN_CUSTODY;

class OrderMadeDateValidatorTest {

    private static final String MESSAGE = "The date of the order cannot not be in the future";

    private final Time time = new FixedTimeConfiguration().stoppedTime();

    private final OrderMadeDateValidator underTest = new OrderMadeDateValidator(time);

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(ORDER_PLACED_CHILD_IN_CUSTODY);
    }

    @Test
    void shouldPassValidationWhenDateIsPresent() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOrderCreatedDate(time.now().toLocalDate())
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldPassValidationWhenDateIsInThePast() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOrderCreatedDate(time.now().toLocalDate().minusDays(1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of());
    }

    @Test
    void shouldFailValidationWhenDateIsFuture() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOrderCreatedDate(time.now().toLocalDate().plusDays(1))
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(MESSAGE));
    }
}
