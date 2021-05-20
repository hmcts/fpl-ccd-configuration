package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.hearing.HearingService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class ApprovalDateBlockPrePopulatorTest {

    private static final Object MANAGE_ORDERS_APPROVED_AT_HEARING_LIST = new Object();
    private static final LocalDateTime END_DATE = LocalDateTime.now();
    private static final CaseData CASE_DATA = CaseData.builder()
        .manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersApprovedAtHearingList(MANAGE_ORDERS_APPROVED_AT_HEARING_LIST)
            .build())
        .build();

    private final HearingService hearingService = mock(HearingService.class);

    private final ApprovalDateBlockPrePopulator underTest = new ApprovalDateBlockPrePopulator(hearingService);

    @Test
    public void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.APPROVAL_DATE);
    }

    @Test
    void doNotPrePopulateWhenNoHearingFound() {
        when(hearingService.findHearing(CASE_DATA,
            MANAGE_ORDERS_APPROVED_AT_HEARING_LIST)).thenReturn(Optional.empty());

        Map<String, Object> actual = underTest.prePopulate(CASE_DATA);

        assertThat(actual).isEqualTo(Map.of());
    }

    @Test
    void prePopulateWhenHearingFound() {
        when(hearingService.findHearing(CASE_DATA, MANAGE_ORDERS_APPROVED_AT_HEARING_LIST))
            .thenReturn(Optional.of(element(
                HearingBooking.builder()
                    .endDate(END_DATE)
                    .build())));

        Map<String, Object> actual = underTest.prePopulate(CASE_DATA);

        assertThat(actual).isEqualTo(Map.of(
            "manageOrdersApprovalDate", END_DATE.toLocalDate()
        ));
    }

}
