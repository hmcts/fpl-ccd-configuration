package uk.gov.hmcts.reform.fpl.service.translations.provider;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class TranslatableNoticeOfProceedingsProviderTest {

    private static final UUID SELECTED_ORDER_ID = UUID.randomUUID();
    private static final UUID UUID_1 = UUID.randomUUID();
    private static final UUID UUID_2 = UUID.randomUUID();

    private static final DocumentReference DOCUMENT_REFERENCE = mock(DocumentReference.class);
    private static final LocalDateTime NOW = LocalDateTime.of(2003, 2, 3, 4, 5);
    private static final DocumentBundle AN_ORDER = mock(DocumentBundle.class);
    private static final DocumentBundle ANOTHER_ORDER = mock(DocumentBundle.class);
    private final Time time = mock(Time.class);

    private final TranslatableNoticeOfProceedingsProvider underTest =
        new TranslatableNoticeOfProceedingsProvider(time);

    @Nested
    class ProvideListItems {

        @Test
        void getItems() {
            List<Element<DocumentBundle>> bundles = List.of(element(mock(DocumentBundle.class)));

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .noticeOfProceedingsBundle(bundles)
                .build());

            assertThat(actual).isEqualTo(bundles);

        }

        @Test
        void getItemsIfEmpty() {

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .noticeOfProceedingsBundle(null)
                .build());

            assertThat(actual).isEqualTo(List.of());
        }
    }

    @Nested
    class ProvideSelectedItemDocument {

        @Test
        void testIfMatchingInCollection() {

            DocumentReference actual = underTest.provideSelectedItemDocument(CaseData.builder()
                    .noticeOfProceedingsBundle(List.of(element(SELECTED_ORDER_ID, DocumentBundle.builder()
                        .document(DOCUMENT_REFERENCE)
                        .build())))
                    .build(),
                SELECTED_ORDER_ID);

            AssertionsForClassTypes.assertThat(actual).isEqualTo(DOCUMENT_REFERENCE);

        }

        @Test
        void testIfNotMatchingInCollection() {
            assertThrows(IllegalArgumentException.class,
                () -> underTest.provideSelectedItemDocument(CaseData.builder()
                        .noticeOfProceedingsBundle(List.of(element(UUID_1, DocumentBundle.builder()
                            .document(DOCUMENT_REFERENCE)
                            .build())))
                        .build(),
                    SELECTED_ORDER_ID));

        }

        @Test
        void testIfCollectionEmpty() {
            assertThrows(IllegalArgumentException.class,
                () -> underTest.provideSelectedItemDocument(CaseData.builder()
                        .noticeOfProceedingsBundle(null)
                        .build(),
                    SELECTED_ORDER_ID));

        }
    }

    @Nested
    class Accept {

        @Test
        void testIfMatchingInCollection() {

            boolean actual = underTest.accept(CaseData.builder()
                    .noticeOfProceedingsBundle(List.of(element(SELECTED_ORDER_ID, DocumentBundle.builder().build())))
                    .build(),
                SELECTED_ORDER_ID);

            AssertionsForClassTypes.assertThat(actual).isTrue();

        }

        @Test
        void testIfNotMatchingInCollection() {
            boolean actual = underTest.accept(CaseData.builder()
                    .noticeOfProceedingsBundle(List.of(element(UUID_1, DocumentBundle.builder().build())))
                    .build(),
                SELECTED_ORDER_ID);

            AssertionsForClassTypes.assertThat(actual).isFalse();

        }

        @Test
        void testIfCollectionEmpty() {
            boolean actual = underTest.accept(CaseData.builder()
                    .noticeOfProceedingsBundle(null)
                    .build(),
                SELECTED_ORDER_ID);

            AssertionsForClassTypes.assertThat(actual).isFalse();
        }
    }

    @Nested
    class ApplyTranslatedOrder {

        @Test
        void applyMatchedOrder() {

            when(time.now()).thenReturn(NOW);

            List<Element<DocumentBundle>> orderCollection = new java.util.ArrayList<>();
            orderCollection.add(element(SELECTED_ORDER_ID, DocumentBundle.builder().build()));

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .noticeOfProceedingsBundle(orderCollection)
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of("noticeOfProceedingsBundle",
                List.of(element(SELECTED_ORDER_ID, DocumentBundle.builder()
                    .translatedDocument(DOCUMENT_REFERENCE)
                    .translationUploadDateTime(NOW)
                    .build())))
            );

        }

        @Test
        void applyMatchedOrderMaintainOrder() {

            when(time.now()).thenReturn(NOW);

            List<Element<DocumentBundle>> orderCollection = new java.util.ArrayList<>();
            orderCollection.add(element(UUID_1, AN_ORDER));
            orderCollection.add(element(SELECTED_ORDER_ID, DocumentBundle.builder().build()));
            orderCollection.add(element(UUID_2, ANOTHER_ORDER));

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .noticeOfProceedingsBundle(orderCollection)
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of("noticeOfProceedingsBundle",
                List.of(
                    element(UUID_1, AN_ORDER),
                    element(SELECTED_ORDER_ID, DocumentBundle.builder()
                        .translatedDocument(DOCUMENT_REFERENCE)
                        .translationUploadDateTime(NOW)
                        .build()),
                    element(UUID_2, ANOTHER_ORDER)
                ))
            );

        }

        @Test
        void applyMatchedOrderNotFound() {

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .noticeOfProceedingsBundle(List.of(
                        element(UUID_1, AN_ORDER),
                        element(UUID_2, ANOTHER_ORDER)
                    ))
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of("noticeOfProceedingsBundle",
                List.of(
                    element(UUID_1, AN_ORDER),
                    element(UUID_2, ANOTHER_ORDER)
                ))
            );

        }
    }


}
