package uk.gov.hmcts.reform.fpl.service.translations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.UploadTranslationsEventData;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.translations.provider.TranslatableListItemProvider;
import uk.gov.hmcts.reform.fpl.service.translations.provider.TranslatableListItemProviders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

class TranslatableItemServiceTest {

    private static final CaseData CASE_DATA = CaseData.builder().build();
    private static final UUID UUID_ID_1 = java.util.UUID.randomUUID();
    private static final UUID UUID_ID_2 = java.util.UUID.randomUUID();
    private static final UUID UUID_ID_3 = java.util.UUID.randomUUID();
    private static final UUID UUID_ID_4 = java.util.UUID.randomUUID();
    private static final DynamicList EMTPY_DYNAMIC_LIST = dynamicListWith(Collections.emptyList());
    private static final String ITEM_1_LABEL = "Item1";
    private static final String ITEM_3_LABEL = "Item3";
    private static final String ITEM_4_LABEL = "Item4";
    private static final byte[] DOCUMENT_BYTES = "GeneratedDocument".getBytes();
    private static final String FILENAME = "Filename";
    private static final Document TEST_DOCUMENT = testDocument();
    private static final Map<String, Object> TRANSFORMED_DATA = Map.of("stuff", "final");
    private static final LocalDateTime TRANSLATION_TIME_1 = LocalDateTime.of(2012, 12, 12, 2, 3);
    private static final LanguageTranslationRequirement TRANSLATION_REQUIREMENTS = ENGLISH_TO_WELSH;

    private final TranslatableListItemProviders providers = mock(TranslatableListItemProviders.class);
    private final TranslatableListItemProvider provider1 = mock(TranslatableListItemProvider.class);
    private final TranslatableListItemProvider provider2 = mock(TranslatableListItemProvider.class);
    private final TranslatableItem translatableItem1 = mock(TranslatableItem.class);
    private final TranslatableItem translatableItem2 = mock(TranslatableItem.class);
    private final TranslatableItem translatableItem3 = mock(TranslatableItem.class);
    private final TranslatableItem translatableItem4 = mock(TranslatableItem.class);
    private final TranslatedDocumentGenerator translatedDocumentGenerator = mock(TranslatedDocumentGenerator.class);
    private final TranslatedFileNameGenerator translatedFileNameGenerator = mock(TranslatedFileNameGenerator.class);
    private final UploadDocumentService uploadDocumentService = mock(UploadDocumentService.class);

    private final TranslatableItemService underTest = new TranslatableItemService(
        providers,
        uploadDocumentService,
        translatedDocumentGenerator,
        translatedFileNameGenerator,
        new DynamicListService(new ObjectMapper())
    );
    private final TranslatableItem translatableItem = mock(TranslatableItem.class);

    @Nested
    class GenerateList {

        @Test
        void returnNothingIfNoProviders() {
            when(providers.getAll()).thenReturn(List.of());

            DynamicList actual = underTest.generateList(CASE_DATA);

            assertThat(actual).isEqualTo(EMTPY_DYNAMIC_LIST);
        }

        @Test
        void returnNothingIfProviderHasNoTranslatableItems() {
            when(providers.getAll()).thenReturn(List.of(provider1));
            when(provider1.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_1, translatableItem1))
            );
            when(translatableItem1.hasBeenTranslated()).thenReturn(true);

            DynamicList actual = underTest.generateList(CASE_DATA);

            assertThat(actual).isEqualTo(EMTPY_DYNAMIC_LIST);
        }

        @Test
        void returnNothingIfMultipleProviderHasNoTranslatableItems() {
            when(providers.getAll()).thenReturn(List.of(provider1, provider2));
            when(provider1.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_1, translatableItem1))
            );
            when(provider2.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_2, translatableItem2))
            );
            when(translatableItem1.hasBeenTranslated()).thenReturn(true);
            when(translatableItem2.hasBeenTranslated()).thenReturn(true);

            DynamicList actual = underTest.generateList(CASE_DATA);

            assertThat(actual).isEqualTo(EMTPY_DYNAMIC_LIST);
        }

        @Test
        void returnNothingIfMultipleProvidersHasMultipleTranslatableItems() {
            when(providers.getAll()).thenReturn(List.of(provider1, provider2));
            when(provider1.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_1, translatableItem1), element(UUID_ID_2, translatableItem2))
            );
            when(provider2.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_3, translatableItem3))
            );
            when(translatableItem1.hasBeenTranslated()).thenReturn(false);
            when(translatableItem1.getTranslationRequirements()).thenReturn(ENGLISH_TO_WELSH);
            when(translatableItem2.hasBeenTranslated()).thenReturn(true);
            when(translatableItem3.hasBeenTranslated()).thenReturn(false);
            when(translatableItem3.getTranslationRequirements()).thenReturn(WELSH_TO_ENGLISH);
            when(translatableItem1.asLabel()).thenReturn(ITEM_1_LABEL);
            when(translatableItem3.asLabel()).thenReturn(ITEM_3_LABEL);

            DynamicList actual = underTest.generateList(CASE_DATA);

            assertThat(actual).isEqualTo(dynamicListWith(List.of(DynamicListElement.builder()
                    .code(UUID_ID_1)
                    .label(ITEM_1_LABEL)
                    .build(),
                DynamicListElement.builder()
                    .code(UUID_ID_3)
                    .label(ITEM_3_LABEL)
                    .build())));
        }

        @Test
        void returnNothingIfMultipleProvidersHasMultipleTranslatableItemsWithNoRequirement() {
            when(providers.getAll()).thenReturn(List.of(provider1, provider2));
            when(provider1.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_1, translatableItem1), element(UUID_ID_2, translatableItem2))
            );
            when(provider2.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_3, translatableItem3), element(UUID_ID_4, translatableItem4))
            );
            when(translatableItem1.hasBeenTranslated()).thenReturn(false);
            when(translatableItem1.getTranslationRequirements()).thenReturn(NO);
            when(translatableItem2.hasBeenTranslated()).thenReturn(true);
            when(translatableItem3.hasBeenTranslated()).thenReturn(false);
            when(translatableItem3.getTranslationRequirements()).thenReturn(ENGLISH_TO_WELSH);
            when(translatableItem3.asLabel()).thenReturn(ITEM_3_LABEL);
            when(translatableItem4.hasBeenTranslated()).thenReturn(false);
            when(translatableItem4.getTranslationRequirements()).thenReturn(null);
            when(translatableItem4.asLabel()).thenReturn(ITEM_4_LABEL);

            DynamicList actual = underTest.generateList(CASE_DATA);

            assertThat(actual).isEqualTo(dynamicListWith(List.of(DynamicListElement.builder()
                .code(UUID_ID_3)
                .label(ITEM_3_LABEL)
                .build())));
        }

    }


    @Nested
    class Finalise {

        @Test
        void testWhenFirstProviderAccepted() {

            CaseData caseData = caseDataWithSelectedUUID(UUID_ID_1);

            when(providers.getAll()).thenReturn(List.of(provider1, provider2));
            when(provider1.accept(caseData, UUID_ID_1)).thenReturn(false);
            when(provider2.accept(caseData, UUID_ID_1)).thenReturn(true);

            mockDocumentGeneration(caseData, TRANSLATION_REQUIREMENTS);

            when(provider2.provideSelectedItem(caseData, UUID_ID_1))
                .thenReturn(translatableItem);
            when(translatableItem.getTranslationRequirements()).thenReturn(TRANSLATION_REQUIREMENTS);

            when(provider2.applyTranslatedOrder(caseData,
                DocumentReference.buildFromDocument(TEST_DOCUMENT),
                UUID_ID_1)).thenReturn(TRANSFORMED_DATA);

            Map<String, Object> actual = underTest.finalise(caseData);

            assertThat(actual).isEqualTo(TRANSFORMED_DATA);

        }

        @Test
        void testWhenFirstProviderInMultipleAccepted() {

            CaseData caseData = caseDataWithSelectedUUID(UUID_ID_1);

            when(providers.getAll()).thenReturn(List.of(provider1, provider2));
            when(provider1.accept(caseData, UUID_ID_1)).thenReturn(true);
            when(provider2.accept(caseData, UUID_ID_1)).thenReturn(true);

            mockDocumentGeneration(caseData, TRANSLATION_REQUIREMENTS);

            when(provider1.provideSelectedItem(caseData, UUID_ID_1))
                .thenReturn(translatableItem);
            when(translatableItem.getTranslationRequirements()).thenReturn(TRANSLATION_REQUIREMENTS);

            when(provider1.applyTranslatedOrder(caseData,
                DocumentReference.buildFromDocument(TEST_DOCUMENT),
                UUID_ID_1)).thenReturn(TRANSFORMED_DATA);

            Map<String, Object> actual = underTest.finalise(caseData);

            assertThat(actual).isEqualTo(TRANSFORMED_DATA);
        }

        @Test
        void testWhenFirstProviderWhenNoAccepted() {

            CaseData caseData = caseDataWithSelectedUUID(UUID_ID_1);

            when(providers.getAll()).thenReturn(List.of(provider1, provider2));
            when(provider1.accept(caseData, UUID_ID_1)).thenReturn(false);
            when(provider2.accept(caseData, UUID_ID_1)).thenReturn(false);

            UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> underTest.finalise(caseData));

            assertThat(exception.getMessage()).isEqualTo(
                "Could not find action to translate item with id \"" + UUID_ID_1 + "\""
            );
        }

        private CaseData caseDataWithSelectedUUID(UUID uuid) {
            return CaseData.builder()
                .uploadTranslationsEventData(UploadTranslationsEventData.builder()
                    .uploadTranslationsRelatedToDocument(selectedId(uuid))
                    .build())
                .build();
        }

        private void mockDocumentGeneration(CaseData caseData, LanguageTranslationRequirement translationRequirements) {
            when(translatedDocumentGenerator.generate(caseData)).thenReturn(DOCUMENT_BYTES);
            when(translatedFileNameGenerator.generate(caseData, translationRequirements)).thenReturn(FILENAME);
            when(uploadDocumentService.uploadDocument(DOCUMENT_BYTES,
                FILENAME,
                RenderFormat.PDF.getMediaType())).thenReturn(TEST_DOCUMENT);
        }
    }

    @Nested
    class GetLastTranslatedItem {

        @Test
        void lastCreatedSingleTranslated() {

            when(providers.getAll()).thenReturn(List.of(provider1, provider2));
            when(provider1.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_1, translatableItem1))
            );
            when(provider2.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_2, translatableItem2))
            );
            when(translatableItem1.hasBeenTranslated()).thenReturn(true);
            when(translatableItem2.hasBeenTranslated()).thenReturn(false);

            Element<? extends TranslatableItem> actual = underTest.getLastTranslatedItem(CASE_DATA);

            assertThat(actual).isEqualTo(element(UUID_ID_1, translatableItem1));
        }

        @Test
        void lastCreatedSingleMultipleTranslatedGetLatest() {

            when(providers.getAll()).thenReturn(List.of(provider1, provider2));
            when(provider1.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_1, translatableItem1))
            );
            when(provider2.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_2, translatableItem2))
            );
            when(translatableItem1.hasBeenTranslated()).thenReturn(true);
            when(translatableItem1.translationUploadDateTime()).thenReturn(TRANSLATION_TIME_1);
            when(translatableItem2.hasBeenTranslated()).thenReturn(true);
            when(translatableItem2.translationUploadDateTime()).thenReturn(TRANSLATION_TIME_1.minusSeconds(1));

            Element<? extends TranslatableItem> actual = underTest.getLastTranslatedItem(CASE_DATA);

            assertThat(actual).isEqualTo(element(UUID_ID_1, translatableItem1));
        }

        @Test
        void lastCreatedSingleMultipleTranslatedGetLatestInverseOrder() {

            when(providers.getAll()).thenReturn(List.of(provider1, provider2));
            when(provider1.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_1, translatableItem1))
            );
            when(provider2.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_2, translatableItem2))
            );
            when(translatableItem1.hasBeenTranslated()).thenReturn(true);
            when(translatableItem1.translationUploadDateTime()).thenReturn(TRANSLATION_TIME_1.minusSeconds(1));
            when(translatableItem2.hasBeenTranslated()).thenReturn(true);
            when(translatableItem2.translationUploadDateTime()).thenReturn(TRANSLATION_TIME_1);

            Element<? extends TranslatableItem> actual = underTest.getLastTranslatedItem(CASE_DATA);

            assertThat(actual).isEqualTo(element(UUID_ID_2, translatableItem2));
        }

        @Test
        void lastCreatedSingleMultipleTranslatedGetLatestAndNull() {

            when(providers.getAll()).thenReturn(List.of(provider1, provider2));
            when(provider1.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_1, translatableItem1))
            );
            when(provider2.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_2, translatableItem2))
            );
            when(translatableItem1.hasBeenTranslated()).thenReturn(true);
            when(translatableItem1.translationUploadDateTime()).thenReturn(null);
            when(translatableItem2.hasBeenTranslated()).thenReturn(true);
            when(translatableItem2.translationUploadDateTime()).thenReturn(TRANSLATION_TIME_1);

            Element<? extends TranslatableItem> actual = underTest.getLastTranslatedItem(CASE_DATA);

            assertThat(actual).isEqualTo(element(UUID_ID_2, translatableItem2));
        }

        @Test
        void exceptionIfNoTranslationFound() {

            when(providers.getAll()).thenReturn(List.of(provider1, provider2));
            when(provider1.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_1, translatableItem1))
            );
            when(provider2.provideListItems(CASE_DATA)).thenReturn(
                List.of(element(UUID_ID_2, translatableItem2))
            );
            when(translatableItem1.hasBeenTranslated()).thenReturn(false);
            when(translatableItem2.hasBeenTranslated()).thenReturn(false);

            UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> underTest.getLastTranslatedItem(CASE_DATA));

            assertThat(exception.getMessage()).isEqualTo("Could not find a translated item");
        }
    }

    private DynamicList selectedId(UUID uuid) {
        return DynamicList.builder().value(DynamicListElement.builder()
            .code(uuid)
            .build()).build();
    }

    private static DynamicList dynamicListWith(List<DynamicListElement> listItems) {
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(listItems)
            .build();
    }


}
