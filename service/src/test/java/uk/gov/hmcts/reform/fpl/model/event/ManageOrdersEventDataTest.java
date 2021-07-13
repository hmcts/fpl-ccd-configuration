package uk.gov.hmcts.reform.fpl.model.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class ManageOrdersEventDataTest {

    private final LocalDate testApprovalDate = LocalDate.of(2007, Month.MARCH, 10);
    private final LocalDateTime testApprovalDateTime = testApprovalDate.atTime(5, 32, 42);

    @Test
    void shouldRetrieveApprovalDateAtStartOfDayIfApprovalDateIsUsed() {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersApprovalDate(testApprovalDate)
            .build();

        assertThat(manageOrdersEventData.getManageOrdersApprovalDateOrDateTime())
            .isEqualTo(testApprovalDate.atStartOfDay());
    }

    @Test
    void shouldRetrieveApprovalDateTimeIfApprovalDateTimeIsUsed() {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .manageOrdersApprovalDateTime(testApprovalDateTime)
            .build();

        assertThat(manageOrdersEventData.getManageOrdersApprovalDateOrDateTime())
            .isEqualTo(testApprovalDateTime);
    }

    @Test
    void shouldReturnNullWhenBothApprovalDateAndApprovalDateTimeAreNull() {
        ManageOrdersEventData manageOrdersEventData = ManageOrdersEventData.builder()
            .build();

        assertThat(manageOrdersEventData.getManageOrdersApprovalDateOrDateTime()).isNull();
    }

}
