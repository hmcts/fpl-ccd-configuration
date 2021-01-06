package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

class CaseManagementOrderTest {
    private static LocalDate NOW  = LocalDate.now();

    @Test
    void shouldReturnTrueIfCaseManagementOrderStatusIsApproved() {
        HearingOrder caseManagementOrder = HearingOrder.builder().status(APPROVED).build();
        assertThat(caseManagementOrder.isRemovable()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = CMOStatus.class, names = {"SEND_TO_JUDGE", "RETURNED", "DRAFT"})
    void shouldReturnFalseIfCaseManagementOrderStatusIsNotApproved(CMOStatus cmoStatus) {
        HearingOrder caseManagementOrder = HearingOrder.builder().status(cmoStatus).build();
        assertThat(caseManagementOrder.isRemovable()).isFalse();
    }

    @Test
    void shouldFormatCaseManagementOrderAsLabel() {
        HearingOrder caseManagementOrder = HearingOrder.builder().dateIssued(NOW).build();

        assertThat(caseManagementOrder.asLabel()).isEqualTo(
            String.format("Case management order - %s", formatLocalDateToString(NOW, "d MMMM yyyy")));
    }
}
