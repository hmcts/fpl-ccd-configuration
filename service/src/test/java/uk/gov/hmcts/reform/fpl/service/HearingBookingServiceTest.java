package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingBookingKeys.HEARING_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.HearingBookingKeys.PAST_HEARING_DETAILS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
class HearingBookingServiceTest {
    private static final LocalDateTime TODAY = LocalDateTime.now();
    private static final UUID[] LIST_UUID = {randomUUID(), randomUUID(), randomUUID()};

    private final HearingBookingService service = new HearingBookingService();

    @Test
    void shouldReturnAnEmptyHearingBookingIfHearingDetailsIsNull() {
        CaseData caseData = CaseData.builder().build();

        List<Element<HearingBooking>> alteredHearingList = service.expandHearingBookingCollection(caseData);

        assertThat(alteredHearingList.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnHearingBookingIfHearingBookingIsPrePopulated() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(HearingBooking.builder().startDate(TODAY).build())))
            .build();

        List<Element<HearingBooking>> hearingList = service.expandHearingBookingCollection(caseData);

        assertThat(hearingList.get(0).getValue().getStartDate()).isEqualTo(TODAY);
    }

    @Test
    void shouldGetMostUrgentHearingBookingFromACollectionOfHearingBookings() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();

        HearingBooking sortedHearingBooking = service.getMostUrgentHearingBooking(hearingBookings);

        assertThat(sortedHearingBooking.getStartDate()).isEqualTo(TODAY);
    }

    @Test
    void shouldGetHearingBookingWhenKeyMatchesHearingBookingElementUUID() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();
        HearingBooking hearingBooking = service.getHearingBookingByUUID(hearingBookings, LIST_UUID[2]);
        assertThat(hearingBooking.getStartDate()).isEqualTo(TODAY);
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
                .value(createDynamicElement(LIST_UUID[0]))
                .listItems(List.of(
                    createDynamicElement(LIST_UUID[0]),
                    createDynamicElement(LIST_UUID[1]),
                    createDynamicElement(LIST_UUID[2])))
                .build();
        }

        private DynamicListElement createDynamicElement(UUID code) {
            return DynamicListElement.builder().code(code).label("").build();
        }
    }

    @Nested
    class SplitPastAndFutureHearings {

        @Test
        void shouldReturnMapWhenNoHearingsHaveBeenCreated() {
            //getter method in caseData populates hearingBookingDetails as empty builder.
            List<Element<HearingBooking>> hearingBooking = emptyHearingBooking();

            assertThat(service.splitPastAndFutureHearings(hearingBooking))
                .isEqualTo(
                    Map.of(HEARING_DETAILS.getKey(), hearingBooking, PAST_HEARING_DETAILS.getKey(), emptyList()));
        }

        @Test
        void shouldReturnMapWhenNoPastHearingsExist() {
            List<Element<HearingBooking>> hearingBooking = newArrayList(hearingElementWithStartDate(+5));

            assertThat(service.splitPastAndFutureHearings(hearingBooking))
                .isEqualTo(
                    Map.of(HEARING_DETAILS.getKey(), hearingBooking, PAST_HEARING_DETAILS.getKey(), emptyList()));
        }

        @Test
        void shouldReturnMapWhenNoFutureHearingsExist() {
            List<Element<HearingBooking>> hearingBooking = newArrayList(hearingElementWithStartDate(-5));

            assertThat(service.splitPastAndFutureHearings(hearingBooking).get(HEARING_DETAILS.getKey())).isNotEmpty();
            assertThat(service.splitPastAndFutureHearings(hearingBooking).get(PAST_HEARING_DETAILS.getKey()))
                .isEqualTo(hearingBooking);
        }

        @Test
        void shouldReturnMapWhenPastHearingsAndFutureHearingsExist() {
            Element<HearingBooking> futureHearingBooking = hearingElementWithStartDate(+5);
            Element<HearingBooking> pastHearingBooking = hearingElementWithStartDate(-5);

            List<Element<HearingBooking>> hearingBookings = newArrayList(futureHearingBooking, pastHearingBooking);

            assertThat(service.splitPastAndFutureHearings(hearingBookings))
                .isEqualTo(Map.of(
                    HEARING_DETAILS.getKey(), List.of(futureHearingBooking),
                    PAST_HEARING_DETAILS.getKey(), List.of(pastHearingBooking)));
        }

        private ArrayList<Element<HearingBooking>> emptyHearingBooking() {
            return newArrayList(element(HearingBooking.builder().build()));
        }
    }

    @Nested
    class RebuildHearingDetailsObject {

        @Test
        void shouldReturnListWhenOnlyFutureHearings() {
            List<Element<HearingBooking>> futureHearingBooking = List.of(hearingElementWithStartDate(+5));

            assertThat(service.rebuildHearingDetailsObject(futureHearingBooking, emptyList()))
                .isEqualTo(futureHearingBooking);
        }

        @Test
        void shouldReturnOrderedListWhenPastAndFutureHearings() {
            List<Element<HearingBooking>> futureHearingBooking = List.of(hearingElementWithStartDate(+5));
            List<Element<HearingBooking>> pastHearingBooking = List.of(hearingElementWithStartDate(-5));

            List<Element<HearingBooking>> expectedHearingBooking = newArrayList();
            expectedHearingBooking.addAll(pastHearingBooking);
            expectedHearingBooking.addAll(futureHearingBooking);

            assertThat(service.rebuildHearingDetailsObject(futureHearingBooking, pastHearingBooking))
                .isEqualTo(expectedHearingBooking);
        }
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return List.of(
            element(LIST_UUID[0], createHearingBooking(TODAY.plusDays(5), TODAY.plusDays(6))),
            element(LIST_UUID[1], createHearingBooking(TODAY.plusDays(2), TODAY.plusDays(3))),
            element(LIST_UUID[2], createHearingBooking(TODAY, TODAY.plusDays(1)))
        );
    }

    private Element<HearingBooking> hearingElementWithStartDate(int i) {
        return element(HearingBooking.builder()
            .startDate(TODAY.plusDays(i))
            .build());
    }
}
