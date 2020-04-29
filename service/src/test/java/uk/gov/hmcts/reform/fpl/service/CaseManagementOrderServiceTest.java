package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.TimeConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    TimeConfiguration.class, CaseManagementOrderService.class, HearingBookingService.class
})
class CaseManagementOrderServiceTest {

    @Autowired
    private Time time;

    @Autowired
    private CaseManagementOrderService service;

    @Test
    void shouldExtractExpectedMapFieldsWhenAllDataIsPresent() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(CaseManagementOrder.builder()
            .schedule(Schedule.builder().build())
            .recitals(emptyList())
            .action(OrderAction.builder().build())
            .build());

        assertThat(data).containsOnlyKeys(SCHEDULE.getKey(), RECITALS.getKey(), ORDER_ACTION.getKey());
    }

    @Test
    void shouldExtractMapFieldsWhenPartialDataIsPresent() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(CaseManagementOrder.builder()
            .schedule(Schedule.builder().build())
            .build());

        assertThat(data).containsOnlyKeys(SCHEDULE.getKey(), RECITALS.getKey(), ORDER_ACTION.getKey());
    }

    @Test
    void shouldExtractMapFieldsWhenCaseManagementOrderIsNull() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(null);

        assertThat(data).containsOnlyKeys(SCHEDULE.getKey(), RECITALS.getKey(), ORDER_ACTION.getKey());
    }

    @Test
    void shouldGetCMOIssueDate() {
        LocalDate expectedIssueDate = LocalDate.now().minusDays(1);
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
            .dateOfIssue(expectedIssueDate.format(DateTimeFormatter.ofPattern(DATE)))
            .build();

        LocalDate issueDate = service.getIssuedDate(caseManagementOrder);

        assertThat(issueDate).isEqualTo(expectedIssueDate);
    }

    @Test
    void shouldGetDefaultCMOIssueDate() {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder().build();

        LocalDate issueDate = service.getIssuedDate(caseManagementOrder);

        assertThat(issueDate).isEqualTo(time.now().toLocalDate());
    }

    @Test
    void shouldGetDefaultCMOIssueDateWhenCMODoesNotExists() {
        LocalDate issueDate = service.getIssuedDate(null);

        assertThat(issueDate).isEqualTo(time.now().toLocalDate());
    }
}
