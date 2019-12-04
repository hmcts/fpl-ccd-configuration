package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ExtendWith(SpringExtension.class)
class HearingBookingServiceTest {

    private final HearingBookingService service = new HearingBookingService();
    private static final LocalDateTime TODAYS_DATE = LocalDateTime.now();

    @Test
    void shouldReturnAnEmptyHearingBookingIfHearingDetailsIsNull() {
        CaseData caseData = CaseData.builder().build();

        List<Element<HearingBooking>> alteredHearingList = service.expandHearingBookingCollection(caseData);

        assertThat(alteredHearingList.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnHearingBookingIfHearingBookingIsPrePopulated() {
        LocalDateTime now = LocalDateTime.now();
        CaseData caseData = CaseData.builder()
            .hearingDetails(
                ImmutableList.of(Element.<HearingBooking>builder()
                    .value(
                        HearingBooking.builder().startDate(now).build())
                    .build()))
            .build();

        List<Element<HearingBooking>> hearingList = service.expandHearingBookingCollection(caseData);

        assertThat(hearingList.get(0).getValue().getStartDate()).isEqualTo(now);
    }

    @Test
    void shouldGetMostUrgentHearingBookingFromACollectionOfHearingBookings() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();

        HearingBooking sortedHearingBooking = service.getMostUrgentHearingBooking(hearingBookings);

        assertThat(sortedHearingBooking.getStartDate()).isEqualTo(TODAYS_DATE);
    }

    @Test
    void shouldGetHearingBookingWhenKeyMatchesHearingBookingElementUUID() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();
        HearingBooking hearingBooking =
            service.getHearingBookingByUUID(hearingBookings, fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2603"));

        assertThat(hearingBooking.getStartDate()).isEqualTo(TODAYS_DATE);
    }

    @Test
    void shouldNotReturnHearingBookingWhenKeyDoesNotMatchHearingBookingElementUUID() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();
        HearingBooking hearingBooking =
            service.getHearingBookingByUUID(hearingBookings, fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2606"));

        assertThat(hearingBooking).isEqualTo(null);
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2601"))
                .value(createHearingBooking(TODAYS_DATE.plusDays(5), TODAYS_DATE.plusDays(6)))
                .build(),
            Element.<HearingBooking>builder()
                .id(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2602"))
                .value(createHearingBooking(TODAYS_DATE.plusDays(2), TODAYS_DATE.plusDays(3)))
                .build(),
            Element.<HearingBooking>builder()
                .id(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2603"))
                .value(createHearingBooking(TODAYS_DATE, TODAYS_DATE.plusDays(1)))
                .build()
        );
    }
}
