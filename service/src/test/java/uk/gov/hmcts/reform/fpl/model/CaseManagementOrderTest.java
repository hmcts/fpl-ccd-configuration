package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;

class CaseManagementOrderTest {

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
        LocalDate issueDate = LocalDate.of(2021, Month.JANUARY, 7);

        HearingOrder caseManagementOrder = HearingOrder.builder().dateIssued(issueDate).build();

        assertThat(caseManagementOrder.asLabel()).isEqualTo("Case management order - 7 January 2021");
    }
}
