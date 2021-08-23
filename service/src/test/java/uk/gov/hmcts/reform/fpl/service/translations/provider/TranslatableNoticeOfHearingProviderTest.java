package uk.gov.hmcts.reform.fpl.service.translations.provider;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class TranslatableNoticeOfHearingProviderTest {
    private static final UUID SELECTED_HEARING_ID = UUID.randomUUID();
    private static final UUID UUID_1 = UUID.randomUUID();
    private static final UUID UUID_2 = UUID.randomUUID();

    private static final DocumentReference DOCUMENT_REFERENCE = mock(DocumentReference.class);
    private static final LocalDateTime NOW = LocalDateTime.of(2003, 2, 3, 4, 5);
    private static final HearingBooking A_HEARING = mock(HearingBooking.class);
    private static final HearingBooking ANOTHER_HEARING = mock(HearingBooking.class);
    private final Time time = mock(Time.class);

    private final TranslatableNoticeOfHearingProvider underTest =
        new TranslatableNoticeOfHearingProvider(time);

    @Nested
    class ProvideListItems {

        @Test
        void getItems() {
            List<Element<HearingBooking>> hearingCollection = List.of(element(mock(HearingBooking.class)));

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .hearingDetails(hearingCollection)
                .build());

            assertThat(actual).isEqualTo(hearingCollection);

        }

        @Test
        void getItemsIfEmpty() {

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .hearingDetails(null)
                .build());

            assertThat(actual).isEqualTo(List.of());
        }
    }

    @Nested
    class ProvideSelectedItemDocument {

        @Test
        void testIfMatchingInCollection() {

            DocumentReference actual = underTest.provideSelectedItemDocument(CaseData.builder()
                    .hearingDetails(List.of(element(SELECTED_HEARING_ID, HearingBooking.builder()
                        .noticeOfHearing(DOCUMENT_REFERENCE)
                        .build())))
                    .build(),
                SELECTED_HEARING_ID);

            assertThat(actual).isEqualTo(DOCUMENT_REFERENCE);

        }

        @Test
        void testIfNotMatchingInCollection() {
            assertThrows(IllegalArgumentException.class,
                () -> underTest.provideSelectedItemDocument(CaseData.builder()
                        .hearingDetails(List.of(element(UUID_1, HearingBooking.builder()
                            .noticeOfHearing(DOCUMENT_REFERENCE)
                            .build())))
                        .build(),
                    SELECTED_HEARING_ID));

        }

        @Test
        void testIfCollectionEmpty() {
            assertThrows(IllegalArgumentException.class,
                () -> underTest.provideSelectedItemDocument(CaseData.builder()
                        .hearingDetails(null)
                        .build(),
                    SELECTED_HEARING_ID));

        }
    }

    @Nested
    class Accept {

        @Test
        void testIfMatchingInCollection() {

            boolean actual = underTest.accept(CaseData.builder()
                    .hearingDetails(List.of(element(SELECTED_HEARING_ID, HearingBooking.builder().build())))
                    .build(),
                SELECTED_HEARING_ID);

            assertThat(actual).isTrue();

        }

        @Test
        void testIfNotMatchingInCollection() {
            boolean actual = underTest.accept(CaseData.builder()
                    .hearingDetails(List.of(element(UUID_1, HearingBooking.builder().build())))
                    .build(),
                SELECTED_HEARING_ID);

            assertThat(actual).isFalse();

        }

        @Test
        void testIfCollectionEmpty() {
            boolean actual = underTest.accept(CaseData.builder()
                    .hearingDetails(null)
                    .build(),
                SELECTED_HEARING_ID);

            assertThat(actual).isFalse();
        }
    }

    @Nested
    class ApplyTranslatedOrder {

        @Test
        void applyMatchedOrder() {

            when(time.now()).thenReturn(NOW);

            List<Element<HearingBooking>> hearingCollection = new java.util.ArrayList<>();
            hearingCollection.add(element(SELECTED_HEARING_ID, HearingBooking.builder().build()));

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .hearingDetails(hearingCollection)
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_HEARING_ID);

            assertThat(actual).isEqualTo(Map.of("hearingDetails",
                List.of(element(SELECTED_HEARING_ID, HearingBooking.builder()
                    .translatedNoticeOfHearing(DOCUMENT_REFERENCE)
                    .translationUploadDateTime(NOW)
                    .build())))
            );

        }

        @Test
        void applyMatchedOrderMaintainOrder() {

            when(time.now()).thenReturn(NOW);

            List<Element<HearingBooking>> hearingCollection = new java.util.ArrayList<>();
            hearingCollection.add(element(UUID_1, A_HEARING));
            hearingCollection.add(element(SELECTED_HEARING_ID, HearingBooking.builder().build()));
            hearingCollection.add(element(UUID_2, ANOTHER_HEARING));

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .hearingDetails(hearingCollection)
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_HEARING_ID);

            assertThat(actual).isEqualTo(Map.of("hearingDetails",
                List.of(
                    element(UUID_1, A_HEARING),
                    element(SELECTED_HEARING_ID, HearingBooking.builder()
                        .translatedNoticeOfHearing(DOCUMENT_REFERENCE)
                        .translationUploadDateTime(NOW)
                        .build()),
                    element(UUID_2, ANOTHER_HEARING)
                ))
            );

        }

        @Test
        void applyMatchedOrderNotFound() {

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .hearingDetails(List.of(
                        element(UUID_1, A_HEARING),
                        element(UUID_2, ANOTHER_HEARING)
                    ))
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_HEARING_ID);

            assertThat(actual).isEqualTo(Map.of("hearingDetails",
                List.of(
                    element(UUID_1, A_HEARING),
                    element(UUID_2, ANOTHER_HEARING)
                ))
            );

        }
    }

}
