package uk.gov.hmcts.reform.fpl.service.translations.provider;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
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

class TranslatableC110AProviderTest {

    private static final UUID SELECTED_ORDER_ID = UUID.randomUUID();

    private static final DocumentReference DOCUMENT_REFERENCE = mock(DocumentReference.class);
    private static final LocalDateTime NOW = LocalDateTime.of(2003, 2, 3, 4, 5);
    private final Time time = mock(Time.class);

    private final TranslatableC110AProvider underTest = new TranslatableC110AProvider(time);

    @Nested
    class ProvideListItems {

        @Test
        void getC110A() {
            C110A c110A = C110A.builder().build();

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .c110A(c110A)
                .build());

            assertThat(actual).isEqualTo(List.of(element(C110A.COLLECTION_ID, c110A)));

        }

        @Test
        void getItemsIfNoC110A() {

            List<Element<? extends TranslatableItem>> actual = underTest.provideListItems(CaseData.builder()
                .c110A(null)
                .build());

            assertThat(actual).isEqualTo(List.of());
        }
    }

    @Nested
    class ProvideSelectedItemDocument {

        @Test
        void testIfMatchingInCollection() {

            TranslatableItem actual = underTest.provideSelectedItem(CaseData.builder()
                    .c110A(C110A.builder()
                        .submittedForm(DOCUMENT_REFERENCE)
                        .build())
                    .build(),
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(C110A.builder()
                .submittedForm(DOCUMENT_REFERENCE)
                .build());
        }
    }

    @Nested
    class Accept {

        @Test
        void testIfMatchingInCollection() {

            boolean actual = underTest.accept(CaseData.builder().build(), C110A.COLLECTION_ID);

            assertThat(actual).isTrue();

        }

        @Test
        void testIfNotMatchingInCollection() {
            boolean actual = underTest.accept(CaseData.builder().build(), SELECTED_ORDER_ID);

            assertThat(actual).isFalse();

        }

    }

    @Nested
    class ApplyTranslatedOrder {

        @Test
        void applyMatchedOrder() {

            when(time.now()).thenReturn(NOW);

            Map<String, Object> actual = underTest.applyTranslatedOrder(CaseData.builder()
                    .c110A(C110A.builder().build())
                    .build(),
                DOCUMENT_REFERENCE,
                SELECTED_ORDER_ID);

            assertThat(actual).isEqualTo(Map.of(
                "translatedSubmittedForm", DOCUMENT_REFERENCE,
                "submittedFormTranslationUploadDateTime", NOW
            ));
        }
    }


}
