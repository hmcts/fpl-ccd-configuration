package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.NextHearing;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.ORDER_ACTION;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookingDynmaicList;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

class CaseManagementOrderServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    private final Time time = () -> NOW;
    private static final LocalDateTime DATE = LocalDateTime.of(2018, 2, 12, 9, 30);

    private CaseManagementOrderService service = new CaseManagementOrderService(time,
        new DateFormatterService(), new HearingBookingService());

    @Test
    void shouldAddDocumentToOrderWhenDocumentExists() throws IOException {
        CaseManagementOrder orderWithDoc = service.addDocument(CaseManagementOrder.builder().build(), document());

        assertThat(orderWithDoc.getOrderDoc()).isEqualTo(buildFromDocument(document()));
    }

    @Test
    void shouldAddActionToOrderWhenActionExists() {
        CaseManagementOrder orderWithDoc = service.addAction(CaseManagementOrder.builder().build(),
            OrderAction.builder().type(SELF_REVIEW).build());

        assertThat(orderWithDoc.getAction()).isEqualTo(OrderAction.builder().type(SELF_REVIEW).build());
    }

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
    void shouldReturnTrueWhenHearingDateIsInFuture() {
        UUID id = randomUUID();
        CaseData caseData = caseDataWithCmo(id)
            .hearingDetails(hearingBookingWithStartDatePlus(id, 1))
            .build();

        assertTrue(service.isHearingDateInFuture(caseData));
    }

    @Test
    void shouldReturnFalseWhenHearingDateIsInPast() {
        UUID id = randomUUID();
        CaseData caseData = caseDataWithCmo(id)
            .hearingDetails(hearingBookingWithStartDatePlus(id, -1))
            .build();

        assertFalse(service.isHearingDateInFuture(caseData));
    }

    private CaseData.CaseDataBuilder caseDataWithCmo(UUID id) {
        return CaseData.builder().cmoToAction(CaseManagementOrder.builder().id(id).build());
    }

    private List<Element<HearingBooking>> hearingBookingWithStartDatePlus(UUID id, int days) {
        return ImmutableList.of(Element.<HearingBooking>builder()
            .id(id)
            .value(HearingBooking.builder()
                .startDate(NOW.plusDays(days))
                .build())
            .build());
    }

    @Test
    void shouldReturnEmptyStringWhenOrderActionIsNotPresentOnCMO() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings(DATE);
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder().build();
        String label = service.createNextHearingDateLabel(caseManagementOrder, hearingBookings);

        assertThat(label).isEqualTo("");
    }

    @Test
    void shouldFormatNextHearingBookingLabelWhenCMOOrderActionContainsMatchingUUID() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings(DATE);
        CaseManagementOrder caseManagementOrder = createCMOWithNextHearing();
        String label = service.createNextHearingDateLabel(caseManagementOrder, hearingBookings);

        assertThat(label).isEqualTo("The next hearing date is on 12 February at 9:30am");
    }

    @Test
    void shouldSetOrderActionNextHearingDateWhenProvidedNextHearingDateList() {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
            .action(OrderAction.builder()
                .type(SEND_TO_ALL_PARTIES)
                .build())
            .build();

        CaseManagementOrder updatedCaseManagementOrder =
            service.addNextHearingToCMO(createHearingBookingDynmaicList(), caseManagementOrder);

        NextHearing nextHearing = updatedCaseManagementOrder.getNextHearing();

        assertThat(nextHearing.getId()).isEqualTo(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"));
        assertThat(nextHearing.getDate()).isEqualTo("15th Dec 2019");
    }

    @Test
    void shouldPreserveCMOWhenNextHearingDateListIsNotProvided() {
        CaseManagementOrder caseManagementOrder = CaseManagementOrder.builder()
            .hearingDate("Test date")
            .build();

        CaseManagementOrder updatedCaseManagementOrder = service.addNextHearingToCMO(null, caseManagementOrder);

        assertThat(updatedCaseManagementOrder.getHearingDate()).isEqualTo("Test date");
    }

    private CaseManagementOrder createCMOWithNextHearing() {
        return CaseManagementOrder.builder()
            .nextHearing(NextHearing.builder()
                .id(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                .build())
            .build();
    }
}
