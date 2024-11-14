package uk.gov.hmcts.reform.fpl.controllers.helper;

import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentStatementV2;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ReviewDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.APPROVED;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.ENGLISH_TO_WELSH;
import static uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement.NO;
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
    public static final UUID UUID_5 = UUID.randomUUID();
    public static final UUID UUID_6 = UUID.randomUUID();
    public static final UUID UUID_7 = UUID.randomUUID();
    public static final UUID UUID_8 = UUID.randomUUID();
    public static final UUID UUID_9 = UUID.randomUUID();
    public static final UUID UUID_10 = UUID.randomUUID();
    public static final UUID UUID_11 = UUID.randomUUID();
    public static final UUID UUID_RESPONDENT = UUID.randomUUID();

    public static final CaseData CASE_DATA_WITH_ALL_ORDERS = CaseData.builder()
        .respondents1(List.of(element(UUID_RESPONDENT, Respondent.builder()
            .party(RespondentParty.builder().lastName("Respondent 1").build())
            .build())))
        .c110A(C110A.builder()
            .submittedFormTranslationRequirements(ENGLISH_TO_WELSH)
            .build())
        .reviewDraftOrdersData(ReviewDraftOrdersData.builder().build())
        .orderCollection(List.of(element(UUID_1, GeneratedOrder.builder()
                .type("Generated order type")
                .dateTimeIssued(LocalDateTime.of(2020, 12, 10, 21, 2, 3))
                .translationRequirements(ENGLISH_TO_WELSH)
                .build()),
            element(UUID_5, GeneratedOrder.builder()
                .type("Generated order type NOT REQUIRING TRANSLATION")
                .dateTimeIssued(LocalDateTime.of(2021, 9, 9, 21, 2, 3))
                .translationRequirements(NO)
                .build())
        ))
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
            .translationRequirements(WELSH_TO_ENGLISH)
            .dateIssued(LocalDate.of(2020, 12, 9))
            .build())))
        .hearingDetails(List.of(
            element(UUID_6, HearingBooking.builder()
                .noticeOfHearing(DocumentReference.builder()
                    .filename("noticeOfHearing.pdf")
                    .build())
                .startDate(LocalDateTime.of(2010, 1, 3, 12, 1, 2))
                .translationRequirements(ENGLISH_TO_WELSH)
                .build()
            )
        ))
        .respStmtList(List.of(
            element(UUID_8, RespondentStatementV2.builder()
                .document(DocumentReference.builder()
                    .filename("respStmt.pdf")
                    .build())
                .translationRequirements(ENGLISH_TO_WELSH)
                .build())))
        .noticeOfProceedingsBundle(List.of(element(UUID_3, DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("noticeo_c6.pdf")
                    .build())
                .translationRequirements(ENGLISH_TO_WELSH)
                .build()
            ), element(UUID_4, DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("noticeo_c6a.pdf")
                    .build())
                .translationRequirements(ENGLISH_TO_WELSH)
                .build()
            )
        ))
        .build();
    public static final DynamicList RENDERED_DYNAMIC_LIST = DynamicList.builder()
        .value(DynamicListElement.EMPTY)
        .listItems(List.of(
            dlElement(UUID_1, "Generated order type - 10 December 2020"),
            dlElement(UUID_2, "Sealed case management order issued on 9 December 2020"),
            dlElement(StandardDirectionOrder.SDO_COLLECTION_ID, "Gatekeeping order - 11 December 2020"),
            dlElement(UUID_3, "Notice of proceedings (C6)"),
            dlElement(UUID_4, "Notice of proceedings (C6A)"),
            dlElement(UrgentHearingOrder.COLLECTION_ID, "Urgent hearing order - 8 December 2020"),
            dlElement(UUID_6, "Notice of hearing - 3 January 2010"),
            dlElement(C110A.COLLECTION_ID, "Application (C110A)"),
            dlElement(UUID_8, "Respondent Statement - respStmt.pdf")
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
