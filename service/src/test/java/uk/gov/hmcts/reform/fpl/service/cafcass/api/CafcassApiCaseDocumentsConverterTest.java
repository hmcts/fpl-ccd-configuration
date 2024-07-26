package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ConfidentialGeneratedOrders;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;
import uk.gov.hmcts.reform.fpl.model.group.C110A;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.AA_PARENT_ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.DocumentsHelper.getDocumentIdFromUrl;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class CafcassApiCaseDocumentsConverterTest extends CafcassApiConverterTestBase {
    private static final ManageDocumentService manageDocumentService = mock(ManageDocumentService.class);

    CafcassApiCaseDocumentsConverterTest() {
        super(new CafcassApiCaseDocumentsConverter(manageDocumentService));
    }

    private List<CafcassApiCaseDocument> getExpectedCafcassApiCaseDocuments(String category, boolean removed,
                                                                            List<DocumentReference> docRefs) {
        return docRefs.stream().map(docRef -> CafcassApiCaseDocument.builder()
                .documentId(getDocumentIdFromUrl(docRef.getUrl()).toString())
                .document_filename(docRef.getFilename())
                .documentCategory(category)
                .removed(removed)
                .build())
            .toList();
    }

    private void testCaseDocument(CaseData caseData, List<DocumentReference> documentReferences,
                                  DocumentType documentType) {
        testCaseDocument(caseData, documentReferences, documentType.getCategory());
    }

    private void testCaseDocument(CaseData caseData, List<DocumentReference> documentReferences, String category) {
        CafcassApiCaseData actual = testConvert(caseData);
        assertThat(actual.getCaseDocuments())
            .containsAll(getExpectedCafcassApiCaseDocuments(category, false, documentReferences));

    }

    @Nested
    class StandardAndUrgentDirectionOrder {
        @Test
        void shouldConvertStandardAndUrgentDirectionOrder() {
            DocumentReference sdoOrder = getTestDocumentReference();
            DocumentReference sdoTranslatedOrder = getTestDocumentReference();
            DocumentReference udoOrder = getTestDocumentReference();
            DocumentReference udoTranslatedOrder = getTestDocumentReference();

            CaseData caseData = CaseData.builder()
                .standardDirectionOrder(StandardDirectionOrder.builder()
                    .orderDoc(sdoOrder)
                    .translatedOrderDoc(sdoTranslatedOrder)
                    .build())
                .urgentDirectionsOrder(StandardDirectionOrder.builder()
                    .orderDoc(udoOrder)
                    .translatedOrderDoc(udoTranslatedOrder)
                    .build())
                .build();

            testCaseDocument(caseData, List.of(sdoOrder, sdoTranslatedOrder, udoOrder, udoTranslatedOrder),
                AA_PARENT_ORDERS);
        }

        @Test
        void shouldReturnEmptyListIfDocumentNotExist() {
            testCaseDocument(
                CaseData.builder()
                    .standardDirectionOrder(StandardDirectionOrder.builder().build())
                    .urgentDirectionsOrder(StandardDirectionOrder.builder().build()).build(),
                List.of(), "draftOrders");
        }

        @Test
        void shouldReturnEmptyListIfNull() {
            testCaseDocument(
                CaseData.builder().standardDirectionOrder(null).urgentDirectionsOrder(null).build(),
                List.of(), "draftOrders");
        }
    }

    @Nested
    class DraftOrders {
        private final static DocumentReference DRAFT_ORDER = getTestDocumentReference();
        private final static DocumentReference DRAFT_ORDER_CONFIDENTIAL = getTestDocumentReference();

        @Test
        void shouldConvertDraftOrdersAndConfidentialOrdersUploadedByChildSolicitor() {
            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = wrapElements(
                HearingOrdersBundle.builder()
                    .orders(wrapElements(HearingOrder.builder().order(DRAFT_ORDER).build()))
                    .build(),
                HearingOrdersBundle.builder()
                    .ordersChild0(wrapElements(HearingOrder.builder()
                        .orderConfidential(DRAFT_ORDER_CONFIDENTIAL).build()))
                    .build());

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(hearingOrdersBundlesDrafts)
                .build();

            testCaseDocument(caseData, List.of(DRAFT_ORDER, DRAFT_ORDER_CONFIDENTIAL), "draftOrders");
        }

        @Test
        void shouldConvertDraftOrdersReviewAndConfidentialOrdersUploadedByChildSolicitor() {
            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftReview = wrapElements(
                HearingOrdersBundle.builder()
                    .orders(wrapElements(HearingOrder.builder().order(DRAFT_ORDER).build()))
                    .build(),
                HearingOrdersBundle.builder()
                    .ordersChild0(wrapElements(HearingOrder.builder()
                        .orderConfidential(DRAFT_ORDER_CONFIDENTIAL).build()))
                    .build());
            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDraftReview(hearingOrdersBundlesDraftReview)
                .build();
            testCaseDocument(caseData, List.of(DRAFT_ORDER, DRAFT_ORDER_CONFIDENTIAL), "draftOrders");
        }

        @Test
        void shouldNotConvertConfidentialOrdersNotUploadedByChildSolicitor() {
            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = wrapElements(
                HearingOrdersBundle.builder()
                    .ordersCTSC(wrapElements(HearingOrder.builder()
                        .orderConfidential(DRAFT_ORDER_CONFIDENTIAL).build()))
                    .build(),
                HearingOrdersBundle.builder()
                    .ordersLA(wrapElements(HearingOrder.builder()
                        .orderConfidential(DRAFT_ORDER_CONFIDENTIAL).build()))
                    .build());

            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftReview = wrapElements(
                HearingOrdersBundle.builder()
                    .ordersResp0(wrapElements(HearingOrder.builder()
                        .orderConfidential(DRAFT_ORDER_CONFIDENTIAL).build()))
                    .build());
            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(hearingOrdersBundlesDrafts)
                .hearingOrdersBundlesDraftReview(hearingOrdersBundlesDraftReview)
                .build();

            testCaseDocument(caseData, List.of(), "draftOrders");
        }

        @Test
        void shouldReturnEmptyListIfNullOrEmpty() {
            testCaseDocument(
                CaseData.builder().hearingOrdersBundlesDrafts(null).hearingOrdersBundlesDraftReview(null).build(),
                List.of(), "draftOrders");
            testCaseDocument(
                CaseData.builder()
                    .hearingOrdersBundlesDrafts(List.of())
                    .hearingOrdersBundlesDraftReview(List.of()).build(),
                List.of(), "draftOrders");
        }
    }

    @Nested
    class ApprovedOrder {
        private static final DocumentReference SEALED_CMO = getTestDocumentReference();
        private static final DocumentReference SEALED_CMO_CONFIDENTIAL = getTestDocumentReference();
        private static final DocumentReference APPROVED_ORDER = getTestDocumentReference();
        private static final DocumentReference APPROVED_ORDER_CONFIDENTIAL = getTestDocumentReference();
        @Test
        void shouldConvertSealedCmosAndOrders() {
            CaseData caseData = CaseData.builder()
                .sealedCMOs(wrapElements(
                    HearingOrder.builder()
                        .order(SEALED_CMO)
                        .build(),
                    HearingOrder.builder()
                        .orderConfidential(SEALED_CMO_CONFIDENTIAL)
                        .build()
                ))
                .orderCollection(wrapElements(GeneratedOrder.builder().document(APPROVED_ORDER).build()))
                .confidentialOrders(ConfidentialGeneratedOrders.builder()
                    .orderCollectionChild0(wrapElements(
                        GeneratedOrder.builder().document(APPROVED_ORDER_CONFIDENTIAL).build()))
                    .build())
                .build();

            testCaseDocument(caseData,
                List.of(SEALED_CMO, SEALED_CMO_CONFIDENTIAL, APPROVED_ORDER, APPROVED_ORDER_CONFIDENTIAL),
                AA_PARENT_ORDERS);
        }

        @Test
        void shouldNotConvertConfidentialOrderNotUploadedByChildSolicitor() {
            CaseData caseData = CaseData.builder()
                .confidentialOrders(ConfidentialGeneratedOrders.builder()
                    .orderCollectionCTSC(wrapElements(
                        GeneratedOrder.builder().document(APPROVED_ORDER_CONFIDENTIAL).build()))
                    .orderCollectionLA(wrapElements(
                        GeneratedOrder.builder().document(APPROVED_ORDER_CONFIDENTIAL).build()))
                    .orderCollectionResp0(wrapElements(
                        GeneratedOrder.builder().document(APPROVED_ORDER_CONFIDENTIAL).build()))
                    .build())
                .build();
            testCaseDocument(caseData, List.of(), AA_PARENT_ORDERS);
        }

        @Test
        void shouldReturnEmptyListIfNullOrEmpty() {
            testCaseDocument(CaseData.builder().build(), List.of(), AA_PARENT_ORDERS);

            testCaseDocument(
                CaseData.builder()
                    .sealedCMOs(List.of()).orderCollection(List.of())
                    .confidentialOrders(ConfidentialGeneratedOrders.builder().build())
                    .build(),
                List.of(), AA_PARENT_ORDERS);

            testCaseDocument(
                CaseData.builder()
                    .confidentialOrders(ConfidentialGeneratedOrders.builder()
                        .orderCollectionResp0(List.of())
                        .orderCollectionChild0(List.of())
                        .build())
                    .build(),
                List.of(), AA_PARENT_ORDERS);
        }
    }

    @Nested
    class OriginalApplications {
        DocumentReference APPLICATION_DOC = getTestDocumentReference();
        DocumentReference APPLICATION_TRANSLATED_DOC = getTestDocumentReference();
        DocumentReference SUPPLEMENT_DOC = getTestDocumentReference();
        Supplement SUPPLEMENT = Supplement.builder().document(SUPPLEMENT_DOC).build();
        DocumentReference SUPPORTING_EVIDENCE_DOC = getTestDocumentReference();
        DocumentReference SUPPORTING_EVIDENCE_TRANSLATEDDOC = getTestDocumentReference();
        SupportingEvidenceBundle supportingEvidence = SupportingEvidenceBundle.builder()
            .document(SUPPORTING_EVIDENCE_DOC)
            .translatedDocument(SUPPORTING_EVIDENCE_TRANSLATEDDOC)
            .build();

        @Test
        void shouldConvertC1OriginalApplicationDocument() {
            CaseData caseData = CaseData.builder()
                .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder()
                    .document(APPLICATION_DOC)
                    .supplementsBundle(wrapElements(SUPPLEMENT))
                    .supportingEvidenceBundle(wrapElements(supportingEvidence))
                    .build())
                .build();

            testCaseDocument(
                caseData,
                List.of(APPLICATION_DOC, SUPPLEMENT_DOC, SUPPORTING_EVIDENCE_DOC, SUPPORTING_EVIDENCE_TRANSLATEDDOC),
                "originalApplications");
        }

        @Test
        void shouldConvertC110AOriginalApplicationDocument() {
            CaseData caseData = CaseData.builder()
                .c110A(C110A.builder()
                    .submittedForm(APPLICATION_DOC)
                    .translatedSubmittedForm(APPLICATION_TRANSLATED_DOC)
                    .supplementDocument(SUPPLEMENT_DOC)
                    .build())
                .build();

            testCaseDocument(
                caseData,
                List.of(APPLICATION_DOC, APPLICATION_TRANSLATED_DOC, SUPPLEMENT_DOC),
                "originalApplications");
        }

        @Test
        void shouldReturnEmptyListIfNullOrEmpty() {
            testCaseDocument(
                CaseData.builder().c110A(null).submittedC1WithSupplement(null).build(),
                List.of(),
                "originalApplications");

            testCaseDocument(
                CaseData.builder().c110A(
                    C110A.builder().build())
                    .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder().build()).build(),
                List.of(),
                "originalApplications");
        }
    }
}
