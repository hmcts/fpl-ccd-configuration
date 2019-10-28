package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.configuration.HearingVenue;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, JsonHearingVenueLookupService.class})
class HearingBookingServiceTest {

    private static final LocalDate TODAYS_DATE = LocalDate.now();
    private final HearingBookingService service = new HearingBookingService();
    @Autowired
    private HearingVenueLookupService lookupService;
    private DynamicList venues;

    @BeforeEach
    void setUp() throws IOException {
        List<HearingVenue> hearingVenues = lookupService.getHearingVenues();
        venues = DynamicList.toDynamicList(hearingVenues);
    }

    @Test
    void shouldReturnAnEmptyHearingBookingIfHearingDetailsIsNull() {
        CaseData caseData = CaseData.builder().build();

        List<Element<HearingBooking>> alteredHearingList = service.expandHearingBookingCollection(caseData, venues);

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

        List<Element<HearingBooking>> hearingList = service.expandHearingBookingCollection(caseData, venues);

        assertThat(hearingList.get(0).getValue().getTime()).isEqualTo("2.30");
    }

    @Test
    void shouldGetMostUrgentHearingBookingFromACollectionOfHearingBookings() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();

        HearingBooking sortedHearingBooking = service.getMostUrgentHearingBooking(hearingBookings);

        assertThat(sortedHearingBooking.getDate()).isEqualTo(TODAYS_DATE);
    }

    @Test
    void shouldUpdateDynamicListIfAlteredVersionIsProvided() throws IOException {
        // Set the first item to be the selected value
        venues.setValue(venues.getListItems().get(0));

        CaseData caseData = CaseData.builder()
            .hearingDetails(ImmutableList.of(Element.<HearingBooking>builder().value(HearingBooking.builder()
                .venueList(venues)
                .build()).build()))
            .build();

        // Add a new hearing and alter the id of the selected one
        List<HearingVenue> hearingVenues = lookupService.getHearingVenues();
        hearingVenues.add(HearingVenue.builder().hearingVenueId(100).title("some venue").build());
        HearingVenue altered = HearingVenue.builder().hearingVenueId(99).title(venues.getValue().getLabel()).build();
        hearingVenues.set(0, altered);

        DynamicList newVenues = DynamicList.toDynamicList(hearingVenues, altered.toDynamicElement());

        List<Element<HearingBooking>> expanded = service.expandHearingBookingCollection(caseData, newVenues);

        DynamicList created = expanded.get(0).getValue().getVenueList();
        assertThat(created.getListItems().size()).isEqualTo(venues.getListItems().size() + 1);
        assertThat(created.getValue()).isEqualTo(altered.toDynamicElement());
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
