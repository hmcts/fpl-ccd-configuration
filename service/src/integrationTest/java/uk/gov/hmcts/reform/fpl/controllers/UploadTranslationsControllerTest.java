package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.enums.docmosis.RenderFormat;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.event.UploadTranslationsEventData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

@WebMvcTest(AddNoteController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadTranslationsControllerTest extends AbstractCallbackTest {

    private static final UUID UUID_1 = UUID.randomUUID();
    private static final UUID UUID_2 = UUID.randomUUID();
    private static final UUID UUID_3 = UUID.randomUUID();
    private static final UUID UUID_4 = UUID.randomUUID();

    private static final CaseData CASE_DATA_WITH_ALL_ORDERS = CaseData.builder()
        .reviewDraftOrdersData(ReviewDraftOrdersData.builder().build())
        .orderCollection(List.of(element(UUID_1, GeneratedOrder.builder()
            .type("Generated order type")
            .dateTimeIssued(LocalDateTime.of(2020, 12, 10, 21, 2, 3))
            .build())))
        .standardDirectionOrder(StandardDirectionOrder.builder()
            .dateOfUpload(LocalDate.of(2020, 12, 11))
            .orderStatus(OrderStatus.SEALED)
            .build())
        .urgentHearingOrder(UrgentHearingOrder.builder()
            .dateAdded(LocalDate.of(2020, 12, 8))
            .build())
        .sealedCMOs(List.of(element(UUID_2, HearingOrder.builder()
            .status(APPROVED)
            .dateIssued(LocalDate.of(2020, 12, 9))
            .build())))
        .noticeOfProceedingsBundle(List.of(element(UUID_3, DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("noticeo_c6.pdf")
                    .build())
                .build()
            ), element(UUID_4, DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("noticeo_c6a.pdf")
                    .build())
                .build()
            )
        ))
        .build();
    private static final DynamicList RENDERED_DYNAMIC_LIST = DynamicList.builder()
        .value(DynamicListElement.EMPTY)
        .listItems(List.of(
            dlElement(UUID_1, "Generated order type - 10 December 2020"),
            dlElement(UUID_2, "Sealed case management order issued on 9 December 2020"),
            dlElement(StandardDirectionOrder.COLLECTION_ID, "Gatekeeping order - 11 December 2020"),
            dlElement(UUID_3, "Notice of proceedings (C6)"),
            dlElement(UUID_4, "Notice of proceedings (C6A)"),
            dlElement(UrgentHearingOrder.COLLECTION_ID, "Urgent hearing order - 8 December 2020")
        )).build();
    private static final DocumentReference TEST_DOCUMENT = DocumentReference.buildFromDocument(testDocument());
    private static final byte[] TRANSLATED_DOC_BYTES = "TranslatedDocumentContent".getBytes();
    private static final byte[] CONVERTED_DOC_BYTES = "ConvertedDocumentContent".getBytes();
    private static final byte[] SEALED_DOC_BYTES = "SealedDocumentContent".getBytes();
    private static final Document UPLOADED_TRANSFORMED_DOCUMENT = testDocument();

    UploadTranslationsControllerTest() {
        super("upload-translations");
    }

    @MockBean
    private DocumentDownloadService documentDownloadService;

    @MockBean
    private DocumentConversionService documentConversionService;

    @MockBean
    private DocumentSealingService documentSealingService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @Test
    void shouldDisplayAllTranslatableOrdersAboutToStart() {

        CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(CASE_DATA_WITH_ALL_ORDERS));

        assertThat(updatedCaseData.getUploadTranslationsEventData().getUploadTranslationsRelatedToDocument()).isEqualTo(
            RENDERED_DYNAMIC_LIST);
    }


    @Test
    void shouldDisplayOriginalDocumentMidEvent() {
        CaseData updatedCaseData = extractCaseData(postMidEvent(
            CASE_DATA_WITH_ALL_ORDERS.toBuilder()
                .uploadTranslationsEventData(UploadTranslationsEventData.builder()
                    .uploadTranslationsRelatedToDocument(RENDERED_DYNAMIC_LIST.toBuilder()
                        .value(dlElement(UUID_3, "Notice of proceedings (C6)"))
                        .build())
                    .build())
                .build(),
            "select-document"
        ));

        assertThat(updatedCaseData.getUploadTranslationsEventData().getUploadTranslationsOriginalDoc()).isEqualTo(
            DocumentReference.builder()
                .filename("noticeo_c6.pdf")
                .build());

    }

    @Test
    void shouldFinaliseDocumentsAboutToSubmit() {
        when(documentDownloadService.downloadDocument(TEST_DOCUMENT.getBinaryUrl())).thenReturn(TRANSLATED_DOC_BYTES);
        when(documentConversionService.convertToPdf(TRANSLATED_DOC_BYTES, TEST_DOCUMENT.getFilename())).thenReturn(
            CONVERTED_DOC_BYTES);
        when(documentSealingService.sealDocument(CONVERTED_DOC_BYTES, SealType.BILINGUAL))
            .thenReturn(SEALED_DOC_BYTES);
        when(uploadDocumentService.uploadDocument(SEALED_DOC_BYTES,
            "noticeo_c6-Welsh.pdf",
            RenderFormat.PDF.getMediaType()))
            .thenReturn(UPLOADED_TRANSFORMED_DOCUMENT);

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(
            CASE_DATA_WITH_ALL_ORDERS.toBuilder()
                .uploadTranslationsEventData(UploadTranslationsEventData.builder()
                    .uploadTranslationsRelatedToDocument(RENDERED_DYNAMIC_LIST.toBuilder()
                        .value(dlElement(UUID_3, "Notice of proceedings (C6)"))
                        .build())
                    .uploadTranslationsOriginalDoc(DocumentReference.builder()
                        .filename("noticeo_c6.pdf")
                        .build())
                    .uploadTranslationsTranslatedDoc(TEST_DOCUMENT)
                    .build())
                .build()
        ));

        assertThat(updatedCaseData).isEqualTo(CASE_DATA_WITH_ALL_ORDERS.toBuilder()
            .noticeOfProceedingsBundle(List.of(element(UUID_3, DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("noticeo_c6.pdf")
                    .build())
                .translatedDocument(DocumentReference.buildFromDocument(UPLOADED_TRANSFORMED_DOCUMENT))
                .translationUploadDateTime(now())
                .build()
            ), element(UUID_4, DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("noticeo_c6a.pdf")
                    .build())
                .build()
            ))).build());

    }

    @Test
    void shouldNotifyPartiesWhenSubmitted() {
        postSubmittedEvent(
            CASE_DATA_WITH_ALL_ORDERS.toBuilder()
                .noticeOfProceedingsBundle(List.of(element(UUID_3, DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("noticeo_c6.pdf")
                        .build())
                    .translatedDocument(DocumentReference.buildFromDocument(UPLOADED_TRANSFORMED_DOCUMENT))
                    .translationUploadDateTime(now())
                    .build()
                ), element(UUID_4, DocumentBundle.builder()
                    .document(DocumentReference.builder()
                        .filename("noticeo_c6a.pdf")
                        .build())
                    .build()
                ))).build());
    }


    private static DynamicListElement dlElement(UUID uuid, String label) {
        return DynamicListElement.builder()
            .code(uuid)
            .label(label)
            .build();
    }

}
