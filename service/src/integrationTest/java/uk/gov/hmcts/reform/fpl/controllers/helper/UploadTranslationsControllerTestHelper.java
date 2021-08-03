package uk.gov.hmcts.reform.fpl.controllers.helper;

import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.WELSH_TO_ENGLISH;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

public class UploadTranslationsControllerTestHelper {

    private UploadTranslationsControllerTestHelper() {
    }

    public static final UUID UUID_1 = UUID.randomUUID();
    public static final UUID UUID_2 = UUID.randomUUID();
    public static final UUID UUID_3 = UUID.randomUUID();
    public static final UUID UUID_4 = UUID.randomUUID();

    public static final CaseData CASE_DATA_WITH_ALL_ORDERS = CaseData.builder()
        .reviewDraftOrdersData(ReviewDraftOrdersData.builder().build())
        .orderCollection(List.of(element(UUID_1, GeneratedOrder.builder()
            .type("Generated order type")
            .dateTimeIssued(LocalDateTime.of(2020, 12, 10, 21, 2, 3))
            .build())))
        .standardDirectionOrder(StandardDirectionOrder.builder()
            .dateOfUpload(LocalDate.of(2020, 12, 11))
            .orderStatus(OrderStatus.SEALED)
            .translationRequirements(ENGLISH_TO_WELSH)
            .build())
        .urgentHearingOrder(UrgentHearingOrder.builder()
            .dateAdded(LocalDate.of(2020, 12, 8))
            .translationRequirements(WELSH_TO_ENGLISH)
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
    public static final DynamicList RENDERED_DYNAMIC_LIST = DynamicList.builder()
        .value(DynamicListElement.EMPTY)
        .listItems(List.of(
            dlElement(UUID_1, "Generated order type - 10 December 2020"),
            dlElement(UUID_2, "Sealed case management order issued on 9 December 2020"),
            dlElement(StandardDirectionOrder.COLLECTION_ID, "Gatekeeping order - 11 December 2020"),
            dlElement(UUID_3, "Notice of proceedings (C6)"),
            dlElement(UUID_4, "Notice of proceedings (C6A)"),
            dlElement(UrgentHearingOrder.COLLECTION_ID, "Urgent hearing order - 8 December 2020")
        )).build();
    public static final DocumentReference TEST_DOCUMENT = DocumentReference.buildFromDocument(testDocument());
    public static final byte[] TRANSLATED_DOC_BYTES = "TranslatedDocumentContent".getBytes();
    public static final byte[] CONVERTED_DOC_BYTES = "ConvertedDocumentContent".getBytes();
    public static final byte[] SEALED_DOC_BYTES = "SealedDocumentContent".getBytes();
    public static final Document UPLOADED_TRANSFORMED_DOCUMENT = testDocument();


    public static DynamicListElement dlElement(UUID uuid, String label) {
        return DynamicListElement.builder()
            .code(uuid)
            .label(label)
            .build();
    }
}
