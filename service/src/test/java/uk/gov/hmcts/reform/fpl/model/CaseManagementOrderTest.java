package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

class CaseManagementOrderTest {
    private static LocalDate NOW  = LocalDate.now();

    @Test
    void shouldReturnTrueIfCaseManagementOrderStatusIsApproved() {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder().status(APPROVED).build();
        assertThat(caseManagementOrder.isRemovable()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = CMOStatus.class, names = {"SEND_TO_JUDGE", "RETURNED", "DRAFT"})
    void shouldReturnTrueIfCaseManagementOrderStatusIsNotApproved(CMOStatus cmoStatus) {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder().status(cmoStatus).build();
        assertThat(caseManagementOrder.isRemovable()).isFalse();
    }

    @Test
    void shouldFormatCaseManagementOrderAsLabel() {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder().dateIssued(NOW).build();

        assertThat(caseManagementOrder.asLabel()).isEqualTo(
            String.format("Case management order - %s", formatLocalDateToString(NOW, "d MMMM yyyy")));
    }
}
