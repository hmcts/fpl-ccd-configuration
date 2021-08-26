package uk.gov.hmcts.reform.fpl.service.translations.provider;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.document.ConfidentialDocumentsSplitter;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.service.translations.provider.decorator.SupportingEvidenceBundleTranslatorDecorator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class TranslatableFurtherEvidenceProviderTest {

    private static final UUID SELECTED_ORDER_ID = UUID.randomUUID();
    private static final UUID UUID_1 = UUID.randomUUID();
    private static final UUID UUID_2 = UUID.randomUUID();

    private static final DocumentReference DOCUMENT_REFERENCE = mock(DocumentReference.class);
    private static final LocalDateTime NOW = LocalDateTime.of(2003, 2, 3, 4, 5);
    private static final SupportingEvidenceBundle A_BUNDLE = mock(SupportingEvidenceBundle.class);
    private static final SupportingEvidenceBundle ANOTHER_BUNDLE = mock(SupportingEvidenceBundle.class);
    private final Time time = mock(Time.class);

    private final TranslatableFurtherEvidenceProvider underTest = new TranslatableFurtherEvidenceProvider(
        new ConfidentialDocumentsSplitter(),
        new SupportingEvidenceBundleTranslatorDecorator(time)
    );

    @Nested
    class ProvideListItems {

        @Test
        void getItems() {
            List<Element<SupportingEvidenceBundle>> bundleCollection =
                List.of(element(mock(SupportingEvidenceBundle.class)));

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .furtherEvidenceDocuments(bundleCollection)
                .build());

            assertThat(actual).isEqualTo(bundleCollection);

        }

        @Test
        void getItemsIfEmpty() {

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .furtherEvidenceDocuments(null)
                .build());

            assertThat(actual).isEqualTo(List.of());
        }
    }

    @Nested
    class ProvideSelectedItemDocument {

        @Test
        void testIfMatchingInCollection() {

            DocumentReference actual = underTest.provideSelectedItemDocument(CaseData.builder()
                    .furtherEvidenceDocuments(List.of(element(SELECTED_ORDER_ID, SupportingEvidenceBundle.builder()
                        .document(DOCUMENT_REFERENCE)
                        .build())))
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(DOCUMENT_REFERENCE);

        }

        @Test
        void testIfNotMatchingInCollection() {
            assertThrows(IllegalArgumentException.class,
                () -> underTest.provideSelectedItemDocument(CaseData.builder()
                        .furtherEvidenceDocuments(List.of(element(UUID_1, SupportingEvidenceBundle.builder()
                            .document(DOCUMENT_REFERENCE)
                            .build())))
                        .build(),
                    SELECTED_ORDER_ID));

        }

        @Test
        void testIfCollectionEmpty() {
            assertThrows(IllegalArgumentException.class,
                () -> underTest.provideSelectedItemDocument(CaseData.builder()
                        .furtherEvidenceDocuments(null)
                        .build(),
                    SELECTED_ORDER_ID));

        }
    }

    @Nested
    class Accept {

        @Test
        void testIfMatchingInCollection() {

            boolean actual = underTest.accept(CaseData.builder()
                    .furtherEvidenceDocuments(List.of(element(SELECTED_ORDER_ID,
                        SupportingEvidenceBundle.builder().build())))
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isTrue();

        }

        @Test
        void testIfNotMatchingInCollection() {
            boolean actual = underTest.accept(CaseData.builder()
                    .furtherEvidenceDocuments(List.of(element(UUID_1, SupportingEvidenceBundle.builder().build())))
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isFalse();

        }

        @Test
        void testIfCollectionEmpty() {
            boolean actual = underTest.accept(CaseData.builder()
                    .furtherEvidenceDocuments(null)
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isFalse();
        }
    }

    @Nested
    class ApplyTranslatedOrder {

        @Test
        void applyMatchedOrder() {

            when(time.now()).thenReturn(NOW);

            List<Element<SupportingEvidenceBundle>> bundleCollection = new ArrayList<>();
            bundleCollection.add(element(SELECTED_ORDER_ID, SupportingEvidenceBundle.builder().build()));

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .furtherEvidenceDocuments(bundleCollection)
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of("furtherEvidenceDocuments",
                List.of(element(SELECTED_ORDER_ID, SupportingEvidenceBundle.builder()
                    .translatedDocument(DOCUMENT_REFERENCE)
                    .translationUploadDateTime(NOW)
                    .build())),
                "furtherEvidenceDocumentsNC",
                List.of(element(SELECTED_ORDER_ID, SupportingEvidenceBundle.builder()
                    .translatedDocument(DOCUMENT_REFERENCE)
                    .translationUploadDateTime(NOW)
                    .build())))
            );

        }

        @Test
        void applyMatchedOrderFilterConfidential() {

            when(time.now()).thenReturn(NOW);

            List<Element<SupportingEvidenceBundle>> bundleCollection = new ArrayList<>();
            bundleCollection.add(element(SELECTED_ORDER_ID,
                SupportingEvidenceBundle.builder().confidential(List.of("CONFIDENTIAL")).build()));

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .furtherEvidenceDocuments(bundleCollection)
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of("furtherEvidenceDocuments",
                List.of(element(SELECTED_ORDER_ID, SupportingEvidenceBundle.builder()
                    .confidential(List.of("CONFIDENTIAL"))
                    .translatedDocument(DOCUMENT_REFERENCE)
                    .translationUploadDateTime(NOW)
                    .build())))
            );

        }

        @Test
        void applyMatchedOrderMaintainOrder() {

            when(time.now()).thenReturn(NOW);

            List<Element<SupportingEvidenceBundle>> bundleCollection = new ArrayList<>();
            bundleCollection.add(element(UUID_1, A_BUNDLE));
            bundleCollection.add(element(SELECTED_ORDER_ID, SupportingEvidenceBundle.builder().build()));
            bundleCollection.add(element(UUID_2, ANOTHER_BUNDLE));

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .furtherEvidenceDocuments(bundleCollection)
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of("furtherEvidenceDocuments",
                List.of(
                    element(UUID_1, A_BUNDLE),
                    element(SELECTED_ORDER_ID, SupportingEvidenceBundle.builder()
                        .translatedDocument(DOCUMENT_REFERENCE)
                        .translationUploadDateTime(NOW)
                        .build()),
                    element(UUID_2, ANOTHER_BUNDLE)
                ),
                "furtherEvidenceDocumentsNC",
                List.of(
                    element(UUID_1, A_BUNDLE),
                    element(SELECTED_ORDER_ID, SupportingEvidenceBundle.builder()
                        .translatedDocument(DOCUMENT_REFERENCE)
                        .translationUploadDateTime(NOW)
                        .build()),
                    element(UUID_2, ANOTHER_BUNDLE)
                ))
            );

        }

        @Test
        void applyMatchedOrderNotFound() {

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .furtherEvidenceDocuments(List.of(
                        element(UUID_1, A_BUNDLE),
                        element(UUID_2, ANOTHER_BUNDLE)
                    ))
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of("furtherEvidenceDocuments",
                List.of(
                    element(UUID_1, A_BUNDLE),
                    element(UUID_2, ANOTHER_BUNDLE)
                ),
                "furtherEvidenceDocumentsNC",
                List.of(
                    element(UUID_1, A_BUNDLE),
                    element(UUID_2, ANOTHER_BUNDLE)
                ))
            );

        }
    }


}
