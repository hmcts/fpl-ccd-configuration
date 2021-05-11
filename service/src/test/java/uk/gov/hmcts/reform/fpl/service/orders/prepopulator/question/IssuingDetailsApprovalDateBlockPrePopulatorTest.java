package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTime;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IssuingDetailsApprovalDateBlockPrePopulatorTest {

    private final Time time = new FixedTime();
    private static final String APPROVAL_DATE_FIELD = "manageOrdersApprovalDate";

    private final IssuingDetailsApprovalDateBlockPrePopulator underTest =
        new IssuingDetailsApprovalDateBlockPrePopulator(time);

    @Test
    void testApprovalDate() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.APPROVAL_DATE);
    }

    @Test
    void testApprovalDateIsPopulatedWithCurrentDate() {
        CaseData caseData = CaseData.builder().build();

        assertThat(underTest.prePopulate(caseData))
            .containsOnly(Map.entry(APPROVAL_DATE_FIELD, time.now().toLocalDate()));
    }

    @Test
    void shouldRetainCurrentlyPopulatedValue() {
        CaseData caseData = CaseData.builder().manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersApprovalDate(time.now().minusMonths(1).toLocalDate()).build()).build();

        assertThat(underTest.prePopulate(caseData)).isEmpty();
    }
}
