package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.events.TranslationUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReferenceWithLanguage;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.SendDocumentRequest;
import uk.gov.hmcts.reform.fpl.service.SendDocumentService;
import uk.gov.hmcts.reform.fpl.service.others.OtherRecipientsInbox;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;
import static uk.gov.hmcts.reform.fpl.enums.ModifiedOrderType.STANDARD_DIRECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.ENGLISH;
import static uk.gov.hmcts.reform.fpl.model.configuration.Language.WELSH;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class TranslationUploadedEventHandlerTest {

    private static final TranslationUploadedEvent EVENT = mock(TranslationUploadedEvent.class);
    private static final CaseData CASE_DATA = mock(CaseData.class);
    private static final Recipient RECIPIENT_1 = mock(Representative.class);
    private static final Representative RECIPIENT_2 = mock(Representative.class);
    private static final Recipient RECIPIENT_3 = mock(Representative.class);
    private static final List<Element<Other>> SELECTED_OTHERS = List.of(element(Other.builder().build()));
    private static final DocumentReference ORIGINAL_DOCUMENT = mock(DocumentReference.class);
    private static final DocumentReference AMENDED_DOCUMENT = mock(DocumentReference.class);

    private final ModifiedDocumentCommonEventHandler modifiedDocumentCommonEventHandler = mock(
        ModifiedDocumentCommonEventHandler.class);
    private final SendDocumentService sendDocumentService = mock(SendDocumentService.class);
    private final OtherRecipientsInbox otherRecipientsInbox = mock(OtherRecipientsInbox.class);

    private final TranslationUploadedEventHandler underTest = new TranslationUploadedEventHandler(
        modifiedDocumentCommonEventHandler,
        sendDocumentService,
        otherRecipientsInbox
    );

    @Test
    void notifyDigitalRepresentatives() {
        underTest.notifyDigitalRepresentatives(EVENT);

        verify(modifiedDocumentCommonEventHandler).notifyDigitalRepresentatives(EVENT);
    }

    @Test
    void notifyEmailRepresentatives() {
        underTest.notifyEmailRepresentatives(EVENT);

        verify(modifiedDocumentCommonEventHandler).notifyEmailRepresentatives(EVENT);
    }

    @Test
    void notifyLocalAuthority() {
        underTest.notifyLocalAuthority(EVENT);

        verify(modifiedDocumentCommonEventHandler).notifyLocalAuthority(EVENT);
    }

    @Test
    void doNotSendOrderByPostIfSDO() {
        underTest.sendOrderByPost(TranslationUploadedEvent.builder()
            .amendedOrderType(STANDARD_DIRECTION_ORDER.getLabel())
            .build());


        verifyNoInteractions(sendDocumentService, modifiedDocumentCommonEventHandler);
    }

    @Test
    void sendOrderByPostIfSDOWithSelectedRecipientsAndWelshToEnglish() {
        List<Recipient> standardRecipients = List.of(RECIPIENT_1);

        when(sendDocumentService.getStandardRecipients(CASE_DATA)).thenReturn(standardRecipients);
        when(otherRecipientsInbox.getNonSelectedRecipients(eq(POST), eq(CASE_DATA), eq(SELECTED_OTHERS), any()))
            .thenReturn(Set.of(RECIPIENT_2));
        when(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(SELECTED_OTHERS)).thenReturn(Set.of());

        underTest.sendOrderByPost(TranslationUploadedEvent.builder()
            .caseData(CASE_DATA)
            .translationRequirements(WELSH_TO_ENGLISH)
            .selectedOthers(SELECTED_OTHERS)
            .originalDocument(ORIGINAL_DOCUMENT)
            .amendedDocument(AMENDED_DOCUMENT)
            .amendedOrderType("type")
            .build());

        verify(sendDocumentService).sendDocuments(
            new SendDocumentRequest(CASE_DATA, List.of(
                DocumentReferenceWithLanguage.builder()
                    .documentReference(ORIGINAL_DOCUMENT)
                    .language(WELSH)
                    .build(),
                DocumentReferenceWithLanguage.builder()
                    .documentReference(AMENDED_DOCUMENT)
                    .language(ENGLISH)
                    .build()
            ), List.of(RECIPIENT_1)));

        verifyNoInteractions(modifiedDocumentCommonEventHandler);
    }

    @Test
    void sendOrderByPostIfSDOWithSelectedRecipientsAndEnglishToWelsh() {
        List<Recipient> standardRecipients = List.of(RECIPIENT_1);

        when(sendDocumentService.getStandardRecipients(CASE_DATA)).thenReturn(standardRecipients);
        when(otherRecipientsInbox.getNonSelectedRecipients(eq(POST), eq(CASE_DATA), eq(SELECTED_OTHERS), any()))
            .thenReturn(Set.of(RECIPIENT_2));
        when(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(SELECTED_OTHERS)).thenReturn(Set.of());

        underTest.sendOrderByPost(TranslationUploadedEvent.builder()
            .caseData(CASE_DATA)
            .translationRequirements(ENGLISH_TO_WELSH)
            .selectedOthers(SELECTED_OTHERS)
            .originalDocument(ORIGINAL_DOCUMENT)
            .amendedDocument(AMENDED_DOCUMENT)
            .amendedOrderType("type")
            .build());

        verify(sendDocumentService).sendDocuments(
            new SendDocumentRequest(CASE_DATA, List.of(
                DocumentReferenceWithLanguage.builder()
                    .documentReference(ORIGINAL_DOCUMENT)
                    .language(ENGLISH)
                    .build(),
                DocumentReferenceWithLanguage.builder()
                    .documentReference(AMENDED_DOCUMENT)
                    .language(WELSH)
                    .build()), List.of(RECIPIENT_1)));
        verifyNoInteractions(modifiedDocumentCommonEventHandler);
    }

    @Test
    void sendOrderByPostIfSDOWithSelectedRecipientsPlusRecipientsWithNoRepresentation() {
        List<Recipient> standardRecipients = List.of(RECIPIENT_1);

        when(sendDocumentService.getStandardRecipients(CASE_DATA)).thenReturn(standardRecipients);
        when(otherRecipientsInbox.getNonSelectedRecipients(eq(POST), eq(CASE_DATA), eq(SELECTED_OTHERS), any()))
            .thenReturn(Set.of(RECIPIENT_2));
        when(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(SELECTED_OTHERS)).thenReturn(Set.of(
            RECIPIENT_3));

        underTest.sendOrderByPost(TranslationUploadedEvent.builder()
            .caseData(CASE_DATA)
            .translationRequirements(ENGLISH_TO_WELSH)
            .selectedOthers(SELECTED_OTHERS)
            .originalDocument(ORIGINAL_DOCUMENT)
            .amendedDocument(AMENDED_DOCUMENT)
            .amendedOrderType("type")
            .build());

        verify(sendDocumentService).sendDocuments(
            new SendDocumentRequest(CASE_DATA,
                List.of(DocumentReferenceWithLanguage.builder()
                        .documentReference(ORIGINAL_DOCUMENT)
                        .language(ENGLISH)
                        .build(),
                    DocumentReferenceWithLanguage.builder()
                        .documentReference(AMENDED_DOCUMENT)
                        .language(WELSH)
                        .build()),
                List.of(RECIPIENT_1, RECIPIENT_3)
            ));

        verifyNoInteractions(modifiedDocumentCommonEventHandler);
    }

    @Test
    void sendOrderByPostIfSDOWithSelectedRecipientsRemovingNonSelected() {
        List<Recipient> standardRecipients = List.of(RECIPIENT_1, RECIPIENT_2);

        when(sendDocumentService.getStandardRecipients(CASE_DATA)).thenReturn(standardRecipients);
        when(otherRecipientsInbox.getNonSelectedRecipients(eq(POST), eq(CASE_DATA), eq(SELECTED_OTHERS), any()))
            .thenReturn(Set.of(RECIPIENT_2));
        when(otherRecipientsInbox.getSelectedRecipientsWithNoRepresentation(SELECTED_OTHERS)).thenReturn(Set.of(
            RECIPIENT_3));

        underTest.sendOrderByPost(TranslationUploadedEvent.builder()
            .caseData(CASE_DATA)
            .translationRequirements(ENGLISH_TO_WELSH)
            .selectedOthers(SELECTED_OTHERS)
            .originalDocument(ORIGINAL_DOCUMENT)
            .amendedDocument(AMENDED_DOCUMENT)
            .amendedOrderType("type")
            .build());

        verify(sendDocumentService).sendDocuments(
            new SendDocumentRequest(CASE_DATA,
                List.of(DocumentReferenceWithLanguage.builder()
                        .documentReference(ORIGINAL_DOCUMENT)
                        .language(ENGLISH)
                        .build(),
                    DocumentReferenceWithLanguage.builder()
                        .documentReference(AMENDED_DOCUMENT)
                        .language(WELSH)
                        .build()),
                List.of(RECIPIENT_1, RECIPIENT_3)
            ));

        verifyNoInteractions(modifiedDocumentCommonEventHandler);
    }

}
