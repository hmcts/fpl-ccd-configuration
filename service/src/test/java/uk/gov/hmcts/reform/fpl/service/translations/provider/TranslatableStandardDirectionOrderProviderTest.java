package uk.gov.hmcts.reform.fpl.service.translations.provider;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class TranslatableStandardDirectionOrderProviderTest {

    private static final UUID SELECTED_ORDER_ID = UUID.randomUUID();

    private static final DocumentReference DOCUMENT_REFERENCE = mock(DocumentReference.class);
    private static final LocalDateTime NOW = LocalDateTime.of(2003, 2, 3, 4, 5);
    private final Time time = mock(Time.class);

    private final TranslatableStandardDirectionOrderProvider underTest =
        new TranslatableStandardDirectionOrderProvider(time);

    @Nested
    class ProvideListItems {

        @Test
        void getSealedOrder() {
            StandardDirectionOrder order = StandardDirectionOrder.builder().orderStatus(OrderStatus.SEALED).build();

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .standardDirectionOrder(order)
                .build());

            assertThat(actual).isEqualTo(List.of(element(StandardDirectionOrder.SDO_COLLECTION_ID, order)));

        }

        @Test
        void getItemsIfNonSealeOrder() {

            StandardDirectionOrder order = StandardDirectionOrder.builder().orderStatus(OrderStatus.PLAIN).build();

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .standardDirectionOrder(order)
                .build());

            assertThat(actual).isEqualTo(List.of());
        }

        @Test
        void getItemsIfNoOrder() {

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .standardDirectionOrder(null)
                .build());

            assertThat(actual).isEqualTo(List.of());
        }
    }

    @Nested
    class ProvideSelectedItemDocument {

        @Test
        void testIfMatchingInCollection() {

            TranslatableItem actual = underTest.provideSelectedItem(CaseData.builder()
                    .standardDirectionOrder(StandardDirectionOrder.builder()
                        .orderDoc(DOCUMENT_REFERENCE)
                        .build())
                    .build(),
                SELECTED_ORDER_ID);

            AssertionsForClassTypes.assertThat(actual).isEqualTo(StandardDirectionOrder.builder()
                .orderDoc(DOCUMENT_REFERENCE)
                .build());
        }
    }

    @Nested
    class Accept {

        @Test
        void testIfMatchingInCollection() {

            boolean actual = underTest.accept(CaseData.builder().build(), StandardDirectionOrder.SDO_COLLECTION_ID);

            AssertionsForClassTypes.assertThat(actual).isTrue();

        }

        @Test
        void testIfNotMatchingInCollection() {
            boolean actual = underTest.accept(CaseData.builder().build(), SELECTED_ORDER_ID);

            AssertionsForClassTypes.assertThat(actual).isFalse();

        }

    }

    @Nested
    class ApplyTranslatedOrder {

        @Test
        void applyMatchedOrder() {

            when(time.now()).thenReturn(NOW);

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .standardDirectionOrder(StandardDirectionOrder.builder().build())
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of("standardDirectionOrder", StandardDirectionOrder.builder()
                .translatedOrderDoc(DOCUMENT_REFERENCE)
                .translationUploadDateTime(NOW)
                .build()));

        }
    }


}
