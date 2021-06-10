package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.DISCHARGE_DETAILS;

public class DischargeOfCareDateValidatorTest {

    private final Time time = new FixedTimeConfiguration().stoppedTime();

    private final DischargeOfCareDateValidator underTest = new DischargeOfCareDateValidator(time);

    @Test
    public void accept() {
        assertThat(underTest.accept()).isEqualTo(DISCHARGE_DETAILS);
    }

    @Test
    void validateIssuedDateBeforeApprovalDate() {
        LocalDate careOrderIssuedDate = time.now().plusDays(1).toLocalDate();

        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersCareOrderIssuedDate(careOrderIssuedDate)
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of("Date of issue cannot be in the future"));
    }
}
