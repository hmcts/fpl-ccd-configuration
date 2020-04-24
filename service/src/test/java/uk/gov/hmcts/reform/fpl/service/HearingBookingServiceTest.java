package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HearingBookingService.class, FixedTimeConfiguration.class})
class HearingBookingServiceTest {
    private static final UUID[] HEARING_IDS = {randomUUID(), randomUUID(), randomUUID(), randomUUID()};

    @Autowired
    private Time time;

    @Autowired
    private HearingBookingService service;

    private LocalDateTime futureDate;
    private LocalDateTime pastDate;
    
    @BeforeEach
    void setUp() {
        futureDate = time.now().plusDays(1);
        pastDate = time.now().minusDays(1);  
    }

    @Test
    void shouldReturnAnEmptyHearingBookingIfHearingDetailsIsNull() {
        CaseData caseData = CaseData.builder().build();

        List<Element<HearingBooking>> alteredHearingList = service.expandHearingBookingCollection(caseData);

        assertThat(alteredHearingList.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnHearingBookingIfHearingBookingIsPrePopulated() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder().startDate(futureDate).build()))
            .build();

        List<Element<HearingBooking>> hearingList = service.expandHearingBookingCollection(caseData);

        assertThat(hearingList.get(0).getValue().getStartDate()).isEqualTo(futureDate);
    }

    @Test
    void shouldGetMostUrgentHearingBookingFromACollectionOfHearingBookings() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();

        HearingBooking sortedHearingBooking = service.getMostUrgentHearingBooking(hearingBookings);

        assertThat(sortedHearingBooking.getStartDate()).isEqualTo(futureDate);
    }

    @Test
    void shouldGetHearingBookingWhenKeyMatchesHearingBookingElementUUID() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();
        HearingBooking hearingBooking = service.getHearingBookingByUUID(hearingBookings, HEARING_IDS[2]);
        assertThat(hearingBooking.getStartDate()).isEqualTo(futureDate);
    }

    @Test
    void shouldReturnNullWhenKeyDoesNotMatchHearingBookingElementUUID() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();
        HearingBooking hearingBooking = service.getHearingBookingByUUID(hearingBookings, randomUUID());

        assertThat(hearingBooking).isNull();
    }

    @Nested
    class GetHearingBooking {

        @Test
        void shouldReturnAEmptyHearingBookingWhenHearingDetailsIsNull() {
            final HearingBooking hearingBooking = service.getHearingBooking(null, createDynamicList());
            assertThat(hearingBooking).isEqualTo(HearingBooking.builder().build());
        }

        @Test
        void shouldReturnAEmptyHearingBookingWhenTheDynamicListIsNull() {
            final HearingBooking hearingBooking = service.getHearingBooking(createHearingBookings(), null);
            assertThat(hearingBooking).isEqualTo(HearingBooking.builder().build());
        }

        @Test
        void shouldReturnAEmptyHearingBookingWhenTheDynamicListValuesCodeIsNull() {
            DynamicList dynamicList = DynamicList.builder().build();
            final HearingBooking hearingBooking = service.getHearingBooking(createHearingBookings(), dynamicList);
            assertThat(hearingBooking).isEqualTo(HearingBooking.builder().build());
        }

        @Test
        void shouldReturnAEmptyHearingBookingWhenHearingDetailsIsEmpty() {
            final HearingBooking hearingBooking = service.getHearingBooking(List.of(), createDynamicList());
            assertThat(hearingBooking).isEqualTo(HearingBooking.builder().build());
        }

        @Test
        void shouldReturnTheFirstHearingBookingWhenTheDynamicListValueCodeMatches() {
            final List<Element<HearingBooking>> hearingBookings = createHearingBookings();
            final HearingBooking hearingBooking = service.getHearingBooking(hearingBookings, createDynamicList());
            assertThat(hearingBooking).isEqualTo(hearingBookings.get(0).getValue());
        }

        private DynamicList createDynamicList() {
            return DynamicList.builder()
                .value(createDynamicElement(HEARING_IDS[0]))
                .listItems(Stream.of(HEARING_IDS)
                    .map(this::createDynamicElement)
                    .collect(toList()))
                .build();
        }

        private DynamicListElement createDynamicElement(UUID code) {
            return DynamicListElement.builder().code(code).label("").build();
        }
    }

    @Nested
    class GetPastHearings {

        @Test
        void shouldReturnEmptyListWhenNoHearingsHaveBeenCreated() {
            assertThat(service.getPastHearings(emptyList())).isEmpty();
        }

        @Test
        void shouldReturnEmptyHearingListWhenNoPastHearingsExist() {
            List<Element<HearingBooking>> hearingBooking = newArrayList(hearingElementWithStartDate(+5));

            assertThat(service.getPastHearings(hearingBooking)).isEmpty();
        }

        @Test
        void shouldReturnEmptyHearingListWhenDateIsToday() {
            List<Element<HearingBooking>> hearingBooking =
                newArrayList(element(HEARING_IDS[0], createHearingBooking(time.now(),
                    time.now().plusDays(6))));

            assertThat(service.getPastHearings(hearingBooking)).isEmpty();
        }

        @Test
        void shouldReturnPopulatedHearingListWhenOnlyPastHearingsExist() {
            List<Element<HearingBooking>> hearingBooking = newArrayList(hearingElementWithStartDate(-5));

            assertThat(service.getPastHearings(hearingBooking)).isEqualTo(hearingBooking);
        }

        @Test
        void shouldReturnOnlyPastHearingsWhenPastAndFutureHearingsExist() {
            Element<HearingBooking> futureHearingBooking = hearingElementWithStartDate(+5);
            Element<HearingBooking> pastHearingBooking = hearingElementWithStartDate(-5);

            List<Element<HearingBooking>> hearingBookings = newArrayList(futureHearingBooking, pastHearingBooking);

            assertThat(service.getPastHearings(hearingBookings)).isEqualTo(List.of(pastHearingBooking));
        }
    }

    @Nested
    class RebuildHearingDetailsObject {

        @Test
        void shouldReturnListWhenOnlyFutureHearings() {
            List<Element<HearingBooking>> futureHearingBooking = List.of(hearingElementWithStartDate(+5));

            assertThat(service.combineHearingDetails(futureHearingBooking, emptyList()))
                .isEqualTo(futureHearingBooking);
        }

        @Test
        void shouldReturnOrderedListWhenPastAndFutureHearings() {
            List<Element<HearingBooking>> futureHearingBooking = List.of(hearingElementWithStartDate(+5));
            List<Element<HearingBooking>> pastHearingBooking = List.of(hearingElementWithStartDate(-5));

            List<Element<HearingBooking>> expectedHearingBooking = newArrayList();
            expectedHearingBooking.addAll(pastHearingBooking);
            expectedHearingBooking.addAll(futureHearingBooking);

            assertThat(service.combineHearingDetails(futureHearingBooking, pastHearingBooking))
                .isEqualTo(expectedHearingBooking);
        }
    }

    @Test
    void shouldReturnFirstHearingWhenHearingExists() {
        assertThat(service.getFirstHearing(createHearingBookings()))
            .isEqualTo(Optional.of(createHearingBooking(pastDate, pastDate.plusDays(1))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnEmptyWhenEmptyListOfHearings(List<Element<HearingBooking>> hearings) {
        assertThat(service.getFirstHearing(hearings)).isEmpty();
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return List.of(
            element(HEARING_IDS[0], createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))),
            element(HEARING_IDS[1], createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3))),
            element(HEARING_IDS[2], createHearingBooking(futureDate, futureDate.plusDays(1))),
            element(HEARING_IDS[3], createHearingBooking(pastDate, pastDate.plusDays(1)))
        );
    }

    private Element<HearingBooking> hearingElementWithStartDate(int daysFromToday) {
        return element(HearingBooking.builder()
            .startDate(futureDate.plusDays(daysFromToday))
            .build());
    }
}
