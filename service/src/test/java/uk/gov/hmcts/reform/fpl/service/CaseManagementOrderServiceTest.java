package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
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
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
class CaseManagementOrderServiceTest {
    private static final LocalDateTime NOW = LocalDateTime.now();
    private final HearingBookingService hearingBookingService = new HearingBookingService();
    private final Time time = () -> NOW;

    private final CaseManagementOrderService service = new CaseManagementOrderService(time, hearingBookingService);

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

        assertThat(data).containsOnlyKeys("schedule", "recitals", "orderAction");
    }

    @Test
    void shouldExtractMapFieldsWhenPartialDataIsPresent() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(CaseManagementOrder.builder()
            .schedule(Schedule.builder().build())
            .build());

        assertThat(data).containsOnlyKeys("schedule", "recitals", "orderAction");
    }

    @Test
    void shouldExtractMapFieldsWhenCaseManagementOrderIsNull() {
        Map<String, Object> data = service.extractMapFieldsFromCaseManagementOrder(null);

        assertThat(data).containsOnlyKeys("schedule", "recitals", "orderAction");
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
}
