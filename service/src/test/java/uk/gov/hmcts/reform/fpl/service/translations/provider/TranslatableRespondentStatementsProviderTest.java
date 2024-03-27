package uk.gov.hmcts.reform.fpl.service.translations.provider;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentStatementV2;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
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

class TranslatableRespondentStatementsProviderTest {

    private static final UUID SELECTED_ORDER_ID = UUID.randomUUID();
    private static final UUID UUID_1 = UUID.randomUUID();
    private static final UUID UUID_2 = UUID.randomUUID();

    private static final DocumentReference DOCUMENT_REFERENCE = mock(DocumentReference.class);
    private static final LocalDateTime NOW = LocalDateTime.of(2003, 2, 3, 4, 5);
    private static final RespondentStatementV2 A_BUNDLE = mock(RespondentStatementV2.class);
    private static final RespondentStatementV2 ANOTHER_BUNDLE = mock(RespondentStatementV2.class);
    private final Time time = mock(Time.class);

    private final TranslatableRespondentStatementsProvider underTest =
        new TranslatableRespondentStatementsProvider(new SupportingEvidenceBundleTranslatorDecorator(time));

    @Nested
    class ProvideListItems {

        @Test
        void getItems() {
            List<Element<RespondentStatementV2>> bundles =
                List.of(element(mock(RespondentStatementV2.class)));

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .respStmtList(bundles)
                .build());

            assertThat(actual).isEqualTo(bundles);
        }

        @Test
        void getItemsFromCTSC() {
            List<Element<RespondentStatementV2>> bundles =
                List.of(element(mock(RespondentStatementV2.class)));

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .respStmtListCTSC(bundles)
                .build());

            assertThat(actual).isEqualTo(bundles);
        }

        @Test
        void getItemsFromLA() {
            List<Element<RespondentStatementV2>> bundles =
                List.of(element(mock(RespondentStatementV2.class)));

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .respStmtListLA(bundles)
                .build());

            assertThat(actual).isEqualTo(bundles);
        }

        @Test
        void getItemsIfEmpty() {

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .respStmtList(null)
                .respStmtListLA(null)
                .respStmtListCTSC(null)
                .build());

            assertThat(actual).isEqualTo(List.of());
        }
    }

    @Nested
    class ProvideSelectedItemDocument {

        @Test
        void testIfMatchingInCollection() {
            TranslatableItem actual = underTest.provideSelectedItem(CaseData.builder()
                    .respStmtList(List.of(
                        element(SELECTED_ORDER_ID, RespondentStatementV2.builder().document(DOCUMENT_REFERENCE).build())
                    ))
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(RespondentStatementV2.builder()
                .document(DOCUMENT_REFERENCE)
                .build());
        }

        @Test
        void testIfMatchingInLACollection() {
            TranslatableItem actual = underTest.provideSelectedItem(CaseData.builder()
                    .respStmtListLA(List.of(
                        element(SELECTED_ORDER_ID, RespondentStatementV2.builder().document(DOCUMENT_REFERENCE).build())
                    ))
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(RespondentStatementV2.builder()
                .document(DOCUMENT_REFERENCE)
                .build());
        }

        @Test
        void testIfMatchingInCTSCCollection() {
            TranslatableItem actual = underTest.provideSelectedItem(CaseData.builder()
                    .respStmtListCTSC(List.of(
                        element(SELECTED_ORDER_ID, RespondentStatementV2.builder().document(DOCUMENT_REFERENCE).build())
                    ))
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(RespondentStatementV2.builder()
                .document(DOCUMENT_REFERENCE)
                .build());
        }

        @Test
        void testIfNotMatchingInCollection() {
            assertThrows(IllegalArgumentException.class,
                () -> underTest.provideSelectedItem(CaseData.builder()
                        .respStmtList(List.of(
                            element(UUID_1, RespondentStatementV2.builder().document(DOCUMENT_REFERENCE).build())
                        ))
                        .build(),
                    SELECTED_ORDER_ID));
        }

        @Test
        void testIfNotMatchingInLACollection() {
            assertThrows(IllegalArgumentException.class,
                () -> underTest.provideSelectedItem(CaseData.builder()
                        .respStmtListLA(List.of(
                            element(UUID_1, RespondentStatementV2.builder().document(DOCUMENT_REFERENCE).build())
                        ))
                        .build(),
                    SELECTED_ORDER_ID));
        }

        @Test
        void testIfNotMatchingInCTSCCollection() {
            assertThrows(IllegalArgumentException.class,
                () -> underTest.provideSelectedItem(CaseData.builder()
                        .respStmtListCTSC(List.of(
                            element(UUID_1, RespondentStatementV2.builder().document(DOCUMENT_REFERENCE).build())
                        ))
                        .build(),
                    SELECTED_ORDER_ID));
        }

        @Test
        void testIfCollectionEmpty() {
            assertThrows(IllegalArgumentException.class,
                () -> underTest.provideSelectedItem(CaseData.builder()
                        .respStmtList(null)
                        .respStmtListLA(null)
                        .respStmtListCTSC(null)
                        .build(),
                    SELECTED_ORDER_ID));
        }
    }

    @Nested
    class Accept {

        @Test
        void testIfMatchingInCollection() {
            boolean actual = underTest.accept(CaseData.builder()
                    .respStmtList(List.of(
                        element(SELECTED_ORDER_ID, RespondentStatementV2.builder().build())
                    )).build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isTrue();
        }

        @Test
        void testIfMatchingInLACollection() {
            boolean actual = underTest.accept(CaseData.builder()
                    .respStmtListLA(List.of(
                        element(SELECTED_ORDER_ID, RespondentStatementV2.builder().build())
                    )).build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isTrue();
        }

        @Test
        void testIfMatchingInCTSCCollection() {
            boolean actual = underTest.accept(CaseData.builder()
                    .respStmtListCTSC(List.of(
                        element(SELECTED_ORDER_ID, RespondentStatementV2.builder().build())
                    )).build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isTrue();
        }

        @Test
        void testIfNotMatchingInCollectionEmpty() {
            boolean actual = underTest.accept(CaseData.builder()
                    .respStmtList(List.of(element(RespondentStatementV2.builder().build())))
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isFalse();
        }

        @Test
        void testIfNotMatchingInLACollectionEmpty() {
            boolean actual = underTest.accept(CaseData.builder()
                    .respStmtListLA(List.of(element(RespondentStatementV2.builder().build())))
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isFalse();
        }

        @Test
        void testIfNotMatchingInCTSCCollectionEmpty() {
            boolean actual = underTest.accept(CaseData.builder()
                    .respStmtListCTSC(List.of(element(RespondentStatementV2.builder().build())))
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isFalse();
        }

        @Test
        void testIfNotMatchingInCollection() {
            boolean actual = underTest.accept(CaseData.builder()
                    .respStmtList(List.of(element(UUID_1, RespondentStatementV2.builder()
                        .document(DOCUMENT_REFERENCE)
                        .build())))
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isFalse();
        }

        @Test
        void testIfNotMatchingInLACollection() {
            boolean actual = underTest.accept(CaseData.builder()
                    .respStmtListLA(List.of(element(UUID_1, RespondentStatementV2.builder()
                        .document(DOCUMENT_REFERENCE)
                        .build())))
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isFalse();
        }

        @Test
        void testIfNotMatchingInCTSCCollection() {
            boolean actual = underTest.accept(CaseData.builder()
                    .respStmtListCTSC(List.of(element(UUID_1, RespondentStatementV2.builder()
                        .document(DOCUMENT_REFERENCE)
                        .build())))
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isFalse();
        }

        @Test
        void testIfCollectionEmpty() {
            boolean actual = underTest.accept(CaseData.builder()
                    .respStmtList(null)
                    .respStmtListCTSC(null)
                    .respStmtListLA(null)
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isFalse();
        }
    }

    @Nested
    class ApplyTranslatedOrder {

        @Test
        void applyMatchedOrderUnderRespStmtList() {
            when(time.now()).thenReturn(NOW);

            List<Element<RespondentStatementV2>> bundles = new ArrayList<>();
            bundles.add(element(SELECTED_ORDER_ID, RespondentStatementV2.builder().build()));

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .respStmtList(bundles)
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of(
                "respStmtListLA", List.of(),
                "respStmtListCTSC", List.of(),
                "respStmtList", List.of(element(SELECTED_ORDER_ID, RespondentStatementV2.builder()
                    .translatedDocument(DOCUMENT_REFERENCE)
                    .translationUploadDateTime(NOW)
                    .build()))
            ));
        }

        @Test
        void applyMatchedOrderUnderRespStmtListLA() {
            when(time.now()).thenReturn(NOW);

            List<Element<RespondentStatementV2>> bundles = new ArrayList<>();
            bundles.add(element(SELECTED_ORDER_ID, RespondentStatementV2.builder().build()));

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .respStmtListLA(bundles)
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of(
                "respStmtList", List.of(),
                "respStmtListCTSC", List.of(),
                "respStmtListLA", List.of(element(SELECTED_ORDER_ID, RespondentStatementV2.builder()
                    .translatedDocument(DOCUMENT_REFERENCE)
                    .translationUploadDateTime(NOW)
                    .build()))
            ));
        }

        @Test
        void applyMatchedOrderUnderRespStmtListCTSC() {
            when(time.now()).thenReturn(NOW);

            List<Element<RespondentStatementV2>> bundles = new ArrayList<>();
            bundles.add(element(SELECTED_ORDER_ID, RespondentStatementV2.builder().build()));

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .respStmtListCTSC(bundles)
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of(
                "respStmtList", List.of(),
                "respStmtListLA", List.of(),
                "respStmtListCTSC", List.of(element(SELECTED_ORDER_ID, RespondentStatementV2.builder()
                    .translatedDocument(DOCUMENT_REFERENCE)
                    .translationUploadDateTime(NOW)
                    .build()))
            ));
        }

        @Test
        void applyMatchedOrderMaintainOrder() {
            when(time.now()).thenReturn(NOW);

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .respStmtList(List.of(
                        element(UUID_1, A_BUNDLE),
                        element(SELECTED_ORDER_ID, RespondentStatementV2.builder().build()),
                        element(UUID_2,ANOTHER_BUNDLE)
                    ))
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of(
                "respStmtListLA", List.of(),
                "respStmtListCTSC", List.of(),
                "respStmtList",
                    List.of(
                        element(UUID_1, A_BUNDLE),
                        element(SELECTED_ORDER_ID, RespondentStatementV2.builder()
                            .translationUploadDateTime(NOW)
                            .translatedDocument(DOCUMENT_REFERENCE)
                            .build()),
                        element(UUID_2, ANOTHER_BUNDLE)
                    )
            ));
        }

        @Test
        void applyMatchedOrderNotFound() {
            when(time.now()).thenReturn(NOW);

            List<Element<RespondentStatementV2>> bundles = new ArrayList<>();

            bundles.add(element(UUID_1, A_BUNDLE));
            bundles.add(element(UUID_2, ANOTHER_BUNDLE));

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .respStmtList(List.of(
                        element(UUID_1, A_BUNDLE),
                        element(UUID_2,ANOTHER_BUNDLE)
                    ))
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of(
                "respStmtListLA", List.of(),
                "respStmtListCTSC", List.of(),
                "respStmtList",
                List.of(
                    element(UUID_1, A_BUNDLE),
                    element(UUID_2, ANOTHER_BUNDLE)
                )
            ));
        }
    }
}
