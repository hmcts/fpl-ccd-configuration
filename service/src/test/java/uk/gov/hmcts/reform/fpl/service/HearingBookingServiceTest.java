package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingDateDynamicElement;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookingDynmaicList;

@ExtendWith(SpringExtension.class)
class HearingBookingServiceTest {

    private static final UUID[] UUIDS = {
        fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"),
        fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"),
        fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31")
    };

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
        HearingBooking hearingBooking = service.getHearingBookingByUUID(hearingBookings, UUIDS[2]);
        assertThat(hearingBooking.getStartDate()).isEqualTo(TODAYS_DATE);
    }

    @Test
    void shouldReturnEmptyHearingBookingWhenKeyDoesNotMatchHearingBookingElementUUID() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();
        HearingBooking hearingBooking =
            service.getHearingBookingByUUID(hearingBookings, fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2606"));

        assertThat(hearingBooking).isEqualTo(HearingBooking.builder().build());
    }

    @Test
    void shouldGetDynamicHearingBookingElementFromDynamicList() {
        HearingDateDynamicElement dateDynamicElement =
            service.getHearingDynamicElement(createHearingBookingDynmaicList());

        assertThat(dateDynamicElement.getId()).isEqualTo(UUIDS[0]);
        assertThat(dateDynamicElement.getDate()).isEqualTo("15th Dec 2019");
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
                .value(createDynamicElement(UUIDS[0]))
                .listItems(List.of(
                    createDynamicElement(UUIDS[0]),
                    createDynamicElement(UUIDS[1]),
                    createDynamicElement(UUIDS[2])
                ))
                .build();
        }

        private DynamicListElement createDynamicElement(UUID code) {
            return DynamicListElement.builder().code(code).label("").build();
        }
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
                .id(UUIDS[0])
                .value(createHearingBooking(TODAYS_DATE.plusDays(5), TODAYS_DATE.plusDays(6)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUIDS[1])
                .value(createHearingBooking(TODAYS_DATE.plusDays(2), TODAYS_DATE.plusDays(3)))
                .build(),
            Element.<HearingBooking>builder()
                .id(UUIDS[2])
                .value(createHearingBooking(TODAYS_DATE, TODAYS_DATE.plusDays(1)))
                .build()
        );
    }
}
