package uk.gov.hmcts.reform.fpl.service.hearing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class HearingServiceTest {

    private static final UUID FIELD_SELECTOR = UUID.randomUUID();
    private static final HearingBooking HEARING_BOOKING = mock(HearingBooking.class);
    private static final UUID ANOTHER_SELECTOR = UUID.randomUUID();
    private static final LocalDateTime NOW = LocalDateTime.of(2020, 12, 10, 2, 3, 4);

    private final Time time = mock(Time.class);

    private final HearingService underTest = new HearingService(time);

    @Nested
    class FindHearing {

        @Test
        void noSelector() {
            Optional<Element<HearingBooking>> actual = underTest.findHearing(CaseData.builder().build(), null);

            assertThat(actual).isEmpty();
        }

        @Test
        void noSelectorSelected() {
            Optional<Element<HearingBooking>> actual = underTest.findHearing(CaseData.builder().build(),
                selectedItem(null)
            );

            assertThat(actual).isEmpty();
        }

        @Test
        void selectorWhenNoBookings() {
            Optional<Element<HearingBooking>> actual = underTest.findHearing(CaseData.builder()
                    .hearingDetails(List.of())
                    .build(),
                selectedItem(FIELD_SELECTOR)
            );

            assertThat(actual).isEmpty();
        }

        @Test
        void selectorWithoutMatchingBooking() {
            Optional<Element<HearingBooking>> actual = underTest.findHearing(CaseData.builder()
                    .hearingDetails(
                        List.of(
                            element(ANOTHER_SELECTOR, HEARING_BOOKING)
                        ))
                    .build(),
                selectedItem(FIELD_SELECTOR)
            );

            assertThat(actual).isEqualTo(Optional.empty());
        }

        @Test
        void selectorMatchingBooking() {
            Optional<Element<HearingBooking>> actual = underTest.findHearing(CaseData.builder()
                    .hearingDetails(List.of(
                        element(FIELD_SELECTOR, HEARING_BOOKING)
                    ))
                    .build(),
                selectedItem(FIELD_SELECTOR)
            );

            assertThat(actual).isEqualTo(Optional.of(element(FIELD_SELECTOR, HEARING_BOOKING)));
        }

        private DynamicList selectedItem(UUID fieldSelector) {
            return DynamicList.builder().value(DynamicListElement.builder().code(fieldSelector).build()).build();
        }
    }

    @Nested
    class FindOnlyHearingsTodayOrInPastNonVacated {

        @BeforeEach
        void setUp() {
            Mockito.when(time.now()).thenReturn(NOW);
        }

        @Test
        void whenNoHearingDetails() {

            List<Element<HearingBooking>> actual = underTest.findOnlyHearingsTodayOrInPastNonVacated(CaseData.builder()
                .hearingDetails(null).cancelledHearingDetails(null).build());

            assertThat(actual).isEqualTo(List.of());
        }

        @Test
        void whenEmptyHearingDetails() {

            List<Element<HearingBooking>> actual = underTest.findOnlyHearingsTodayOrInPastNonVacated(CaseData.builder()
                .hearingDetails(List.of()).cancelledHearingDetails(List.of()).build());

            assertThat(actual).isEqualTo(List.of());
        }

        @Test
        void returnInThePast() {
            Element<HearingBooking> hearing = hearing(NOW.minusSeconds(1));

            List<Element<HearingBooking>> actual = underTest.findOnlyHearingsTodayOrInPastNonVacated(CaseData.builder()
                .hearingDetails(List.of(
                    hearing
                )).build());

            assertThat(actual).isEqualTo(List.of(hearing));
        }

        @Test
        void returnInThePastCancelled() {
            Element<HearingBooking> hearing = hearing(NOW.minusSeconds(1));

            List<Element<HearingBooking>> actual = underTest.findOnlyHearingsTodayOrInPastNonVacated(CaseData.builder()
                .cancelledHearingDetails(List.of(
                    hearing
                )).build());

            assertThat(actual).isEqualTo(List.of(hearing));
        }

        @Test
        void returnNow() {
            Element<HearingBooking> hearing = hearing(NOW);

            List<Element<HearingBooking>> actual = underTest.findOnlyHearingsTodayOrInPastNonVacated(CaseData.builder()
                .hearingDetails(List.of(
                    hearing
                )).build());

            assertThat(actual).isEqualTo(List.of(hearing));
        }

        @ParameterizedTest
        @EnumSource(value = HearingStatus.class,
            names = {"VACATED", "VACATED_AND_RE_LISTED", "VACATED_TO_BE_RE_LISTED"})
        void doNotReturnIfStatus(HearingStatus hearingStatus) {
            Element<HearingBooking> hearing = hearing(NOW.minusSeconds(1), hearingStatus);

            List<Element<HearingBooking>> actual = underTest.findOnlyHearingsTodayOrInPastNonVacated(CaseData.builder()
                .hearingDetails(List.of(
                    hearing
                )).build());

            assertThat(actual).isEqualTo(List.of());
        }

        @ParameterizedTest
        @NullSource
        @EnumSource(value = HearingStatus.class,
            names = {"ADJOURNED", "ADJOURNED_TO_BE_RE_LISTED", "ADJOURNED_AND_RE_LISTED"})
        void returnIfStatus(HearingStatus hearingStatus) {
            Element<HearingBooking> hearing = hearing(NOW.minusSeconds(1), hearingStatus);

            List<Element<HearingBooking>> actual = underTest.findOnlyHearingsTodayOrInPastNonVacated(CaseData.builder()
                .hearingDetails(List.of(
                    hearing
                )).build());

            assertThat(actual).isEqualTo(List.of(hearing));
        }

        @Test
        void doReturnInFutureIfJustBeforeTodayMidnight() {
            Element<HearingBooking> hearing = hearing(NOW.toLocalDate().plusDays(1).atStartOfDay().minusSeconds(1));

            List<Element<HearingBooking>> actual = underTest.findOnlyHearingsTodayOrInPastNonVacated(CaseData.builder()
                .hearingDetails(List.of(
                    hearing
                )).build());

            assertThat(actual).isEqualTo(List.of(hearing));
        }

        @Test
        void doNotReturnInFutureIfAfterTodayMidnight() {
            Element<HearingBooking> hearing = hearing(NOW.toLocalDate().plusDays(1).atStartOfDay());

            List<Element<HearingBooking>> actual = underTest.findOnlyHearingsTodayOrInPastNonVacated(CaseData.builder()
                .hearingDetails(List.of(
                    hearing
                )).build());

            assertThat(actual).isEqualTo(List.of());
        }

        private Element<HearingBooking> hearing(LocalDateTime localDateTime) {
            return element(FIELD_SELECTOR, HearingBooking.builder().endDate(localDateTime).build());
        }

        private Element<HearingBooking> hearing(LocalDateTime localDateTime, HearingStatus status) {
            return element(FIELD_SELECTOR, HearingBooking.builder().endDate(localDateTime).status(status).build());
        }
    }
}
