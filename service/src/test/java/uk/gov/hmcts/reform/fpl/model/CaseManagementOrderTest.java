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
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;

class CaseManagementOrderTest {

    @ParameterizedTest
    @EnumSource(value = CMOStatus.class, names = {"APPROVED", "DRAFT", "RETURNED", "SEND_TO_JUDGE"})
    void shouldReturnTrueIfCaseManagementOrderStatusIsApprovedOrDraft(CMOStatus cmoStatus) {
        HearingOrder caseManagementOrder = HearingOrder.builder().type(DRAFT_CMO).status(cmoStatus).build();
        assertThat(caseManagementOrder.isRemovable()).isTrue();
    }

    @Test
    void shouldFormatSealedCaseManagementOrderAsLabelWithDateIssued() {
        LocalDate issueDate = LocalDate.of(2021, Month.JANUARY, 7);

        HearingOrder caseManagementOrder = HearingOrder.builder().status(APPROVED).dateIssued(issueDate).build();

        assertThat(caseManagementOrder.asLabel()).isEqualTo("Sealed case management order issued on 7 January 2021");
    }

    @Test
    void shouldFormatDraftCaseManagementOrderAsLabelWithDateSent() {
        LocalDate dateSent = LocalDate.of(2021, Month.JANUARY, 10);

        HearingOrder caseManagementOrder = HearingOrder.builder().status(DRAFT).dateSent(dateSent).build();

        assertThat(caseManagementOrder.asLabel()).isEqualTo("Draft case management order sent on 10 January 2021");
    }
}
