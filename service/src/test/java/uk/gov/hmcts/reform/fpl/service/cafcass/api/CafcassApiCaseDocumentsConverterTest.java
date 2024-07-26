package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
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
            testConvert(CaseData.builder()
                    .standardDirectionOrder(StandardDirectionOrder.builder().build())
                    .urgentDirectionsOrder(StandardDirectionOrder.builder().build()).build(),
                CafcassApiCaseData.builder().caseDocuments(List.of()).build());
        }

        @Test
        void shouldReturnEmptyListIfNull() {
            testConvert(CaseData.builder().standardDirectionOrder(null).urgentDirectionsOrder(null).build(),
                CafcassApiCaseData.builder().caseDocuments(List.of()).build());
        }
    }

    @Nested
    class DraftOrders {
        private final static DocumentReference DRAFT_ORDER = getTestDocumentReference();
        private final static DocumentReference DRAFT_ORDER_CONFIDENTIAL = getTestDocumentReference();
        private final static DocumentReference DRAFT_ORDER_REVIEW = getTestDocumentReference();
        private final static DocumentReference DRAFT_ORDER_REVIEW_CONFIDENTIAL = getTestDocumentReference();

        @Test
        void shouldConvertDraftOrders() {
            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = wrapElements(
                HearingOrdersBundle.builder()
                    .orders(wrapElements(HearingOrder.builder().order(DRAFT_ORDER).build()))
                    .build(),
                HearingOrdersBundle.builder()
                    .ordersChild0(wrapElements(HearingOrder.builder()
                        .orderConfidential(DRAFT_ORDER_CONFIDENTIAL).build()))
                    .build());
            List<Element<HearingOrdersBundle>> hearingOrdersBundlesDraftReview = wrapElements(
                HearingOrdersBundle.builder()
                    .orders(wrapElements(HearingOrder.builder().order(DRAFT_ORDER_REVIEW).build()))
                    .build(),
                HearingOrdersBundle.builder()
                    .ordersResp0(wrapElements(HearingOrder.builder()
                        .orderConfidential(DRAFT_ORDER_REVIEW_CONFIDENTIAL).build()))
                    .build());

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(hearingOrdersBundlesDrafts)
                .hearingOrdersBundlesDraftReview(hearingOrdersBundlesDraftReview)
                .build();

            testCaseDocument(caseData, List.of(DRAFT_ORDER, DRAFT_ORDER_CONFIDENTIAL, DRAFT_ORDER_REVIEW),
                "draftOrders");
        }

        @Test
        void shouldReturnEmptyListIfNullOrEmpty() {
            CafcassApiCaseData expected = CafcassApiCaseData.builder().caseDocuments(List.of()).build();
            testConvert(CaseData.builder().hearingOrdersBundlesDrafts(null)
                    .hearingOrdersBundlesDraftReview(null).build(), expected);
            testConvert(
                CaseData.builder().hearingOrdersBundlesDrafts(List.of())
                    .hearingOrdersBundlesDraftReview(List.of()).build(), expected);
        }
    }
}
