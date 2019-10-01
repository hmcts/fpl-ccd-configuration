package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ExtendWith(SpringExtension.class)
class HearingBookingServiceTest {

    private final HearingBookingService service = new HearingBookingService();
    private static final LocalDate TODAYS_DATE = LocalDate.now();

    @Test
    void shouldReturnAnEmptyHearingBookingIfHearingDetailsIsNull() {
        CaseData caseData = CaseData.builder().build();

        List<Element<HearingBooking>> alteredHearingList = service.expandHearingBookingCollection(caseData);

        assertThat(alteredHearingList.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnHearingBookingIfHearingBookingIsPrePopulated() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(
                ImmutableList.of(Element.<HearingBooking>builder()
                    .value(
                        HearingBooking.builder().time("2.30").build())
                    .build()))
            .build();

        List<Element<HearingBooking>> hearingList = service.expandHearingBookingCollection(caseData);

        assertThat(hearingList.get(0).getValue().getTime()).isEqualTo("2.30");
    }

    @Test
    void shouldGetMostUrgentHearingBookingFromACollectionOfHearingBookings() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();

        CaseData caseData = CaseData.builder().hearingDetails(hearingBookings).build();
        HearingBooking sortedHearingBooking = service.getMostUrgentHearingBooking(caseData);

        assertThat(sortedHearingBooking.getDate()).isEqualTo(TODAYS_DATE);
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(LocalDate.now().plusDays(5))).build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(LocalDate.now().plusDays(2))).build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(createHearingBooking(TODAYS_DATE)).build()
        );
    }
}
