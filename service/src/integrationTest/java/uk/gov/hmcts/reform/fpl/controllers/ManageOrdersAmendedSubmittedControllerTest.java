package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.OrderOperation;
import uk.gov.hmcts.reform.fpl.model.order.UrgentHearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.EventService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.ccd.CCDConcurrencyHelper;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.enums.State.SUBMITTED;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageOrdersAmendedSubmittedControllerTest extends AbstractCallbackTest {

    private static final LocalDate FIXED_DATE = LocalDate.of(420, 6, 9);

    private static final byte[] DOCUMENT_TO_STAMP_BINARIES = readBytes("documents/document.pdf");
    private static final byte[] STAMPED_BINARIES = readBytes("documents/document-amended.pdf");

    private static final Document AMENDED_DOCUMENT = testDocument();

    private static final DocumentReference AMENDED_ORDER = buildFromDocument(AMENDED_DOCUMENT);
    private static final DocumentReference DOCUMENT_TO_STAMP = testDocumentReference("uploaded.pdf");
    private static final DocumentReference ORDER_DOCUMENT = testDocumentReference("order.pdf");
    private static final DocumentReference CMO_DOCUMENT = testDocumentReference("cmo.pdf");
    private static final DocumentReference SDO_DOCUMENT = testDocumentReference("sdo.pdf");
    private static final DocumentReference UHO_DOCUMENT = testDocumentReference("uho.pdf");

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CMO_ID = UUID.randomUUID();
    private static final UUID SDO_ID = StandardDirectionOrder.COLLECTION_ID;
    private static final UUID UHO_ID = UrgentHearingOrder.COLLECTION_ID;

    private static final HearingOrder CMO = HearingOrder.builder().order(CMO_DOCUMENT)
        .others(emptyList()).build();
    private static final UrgentHearingOrder UHO = UrgentHearingOrder.builder().order(UHO_DOCUMENT)
        .others(emptyList()).build();
    private static final StandardDirectionOrder SDO = StandardDirectionOrder.builder().orderDoc(SDO_DOCUMENT)
        .others(emptyList()).build();
    private static final GeneratedOrder ORDER = GeneratedOrder.builder().document(ORDER_DOCUMENT)
        .others(emptyList()).build();
    private static final String MEDIA_TYPE = "application/pdf";
    public static final String INTERNAL_CHANGE_MANAGE_ORDER = "internal-change-manage-order";

    @MockBean
    private DocumentDownloadService downloadService;
    @MockBean
    private UploadDocumentService uploadService;
    @MockBean
    private Time time; // mocking to ensure time that is stamped into the doc matches the one in the test doc
    @MockBean
    private CCDConcurrencyHelper concurrencyHelper;
    @MockBean
    private EventService eventPublisher;
    @Captor
    private ArgumentCaptor<byte[]> documentBinaries;
    @Captor
    private ArgumentCaptor<Map<String, Object>> updateData;

    ManageOrdersAmendedSubmittedControllerTest() {
        super("manage-orders");
    }

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(LocalDateTime.of(FIXED_DATE, LocalTime.NOON));
        when(downloadService.downloadDocument(DOCUMENT_TO_STAMP.getBinaryUrl()))
            .thenReturn(DOCUMENT_TO_STAMP_BINARIES);
        when(uploadService.uploadDocument(any(), any(), any())).thenReturn(AMENDED_DOCUMENT);
    }

    @Test
    void shouldAmendGeneratedOrder() {
        CaseData caseData = buildCaseData(ORDER_ID, ORDER_DOCUMENT);
        when(concurrencyHelper.startEvent(any(), any())).thenReturn(StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(INTERNAL_CHANGE_MANAGE_ORDER)
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        verify(uploadService).uploadDocument(documentBinaries.capture(), eq("amended_order.pdf"), eq(MEDIA_TYPE));
        assertThat(documentBinaries.getValue()).isEqualTo(STAMPED_BINARIES);
        verify(concurrencyHelper).startEvent(eq(caseData.getId()), eq(INTERNAL_CHANGE_MANAGE_ORDER));
        verify(concurrencyHelper).submitEvent(any(), eq(caseData.getId()), updateData.capture());

        Map<String, Object> updates = asCaseDetails(caseData).getData();
        updates.putAll(updateData.getValue());

        CaseData responseData = extractCaseData(updates);
        GeneratedOrder updatedOrder = ORDER.toBuilder().amendedDate(FIXED_DATE).document(AMENDED_ORDER).build();
        assertOrders(responseData, updatedOrder, CMO, SDO, UHO);
    }

    @Test
    void shouldAmendCMO() {
        CaseData caseData = buildCaseData(CMO_ID, CMO_DOCUMENT);
        when(concurrencyHelper.startEvent(any(), any())).thenReturn(StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(INTERNAL_CHANGE_MANAGE_ORDER)
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        verify(uploadService).uploadDocument(documentBinaries.capture(), eq("amended_cmo.pdf"), eq(MEDIA_TYPE));
        assertThat(documentBinaries.getValue()).isEqualTo(STAMPED_BINARIES);
        verify(concurrencyHelper).startEvent(eq(caseData.getId()), eq(INTERNAL_CHANGE_MANAGE_ORDER));
        verify(concurrencyHelper).submitEvent(any(), eq(caseData.getId()), updateData.capture());

        Map<String, Object> updates = asCaseDetails(caseData).getData();
        updates.putAll(updateData.getValue());

        CaseData responseData = extractCaseData(updates);
        HearingOrder updatedCMO = CMO.toBuilder().amendedDate(FIXED_DATE).order(AMENDED_ORDER).build();
        assertOrders(responseData, ORDER, updatedCMO, SDO, UHO);
    }

    @Test
    void shouldAmendSDO() {
        CaseData caseData = buildCaseData(SDO_ID, SDO_DOCUMENT);
        when(concurrencyHelper.startEvent(any(), any())).thenReturn(StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(INTERNAL_CHANGE_MANAGE_ORDER)
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        verify(uploadService).uploadDocument(documentBinaries.capture(), eq("amended_sdo.pdf"), eq(MEDIA_TYPE));
        assertThat(documentBinaries.getValue()).isEqualTo(STAMPED_BINARIES);
        verify(concurrencyHelper).startEvent(eq(caseData.getId()), eq(INTERNAL_CHANGE_MANAGE_ORDER));
        verify(concurrencyHelper).submitEvent(any(), eq(caseData.getId()), updateData.capture());

        Map<String, Object> updates = asCaseDetails(caseData).getData();
        updates.putAll(updateData.getValue());

        CaseData responseData = extractCaseData(updates);
        StandardDirectionOrder updatedSDO = SDO.toBuilder().amendedDate(FIXED_DATE).orderDoc(AMENDED_ORDER).build();
        assertOrders(responseData, ORDER, CMO, updatedSDO, UHO);
    }

    @Test
    void shouldAmendUHO() {
        CaseData caseData = buildCaseData(UHO_ID, UHO_DOCUMENT);
        when(concurrencyHelper.startEvent(any(), any())).thenReturn(StartEventResponse.builder()
            .caseDetails(asCaseDetails(caseData))
            .eventId(INTERNAL_CHANGE_MANAGE_ORDER)
            .token("token")
            .build());

        postSubmittedEvent(caseData);

        verify(uploadService).uploadDocument(documentBinaries.capture(), eq("amended_uho.pdf"), eq(MEDIA_TYPE));
        assertThat(documentBinaries.getValue()).isEqualTo(STAMPED_BINARIES);
        verify(concurrencyHelper).startEvent(eq(caseData.getId()), eq(INTERNAL_CHANGE_MANAGE_ORDER));
        verify(concurrencyHelper).submitEvent(any(), eq(caseData.getId()), updateData.capture());

        Map<String, Object> updates = asCaseDetails(caseData).getData();
        updates.putAll(updateData.getValue());

        CaseData responseData = extractCaseData(updates);
        UrgentHearingOrder updatedUHO = UHO.toBuilder().amendedDate(FIXED_DATE).order(AMENDED_ORDER).build();
        assertOrders(responseData, ORDER, CMO, SDO, updatedUHO);
    }

    private void assertOrders(CaseData caseData, GeneratedOrder order, HearingOrder cmo,
                              StandardDirectionOrder sdo, UrgentHearingOrder uho) {
        assertThat(caseData.getOrderCollection()).isEqualTo(List.of(element(ORDER_ID, order)));
        assertThat(caseData.getSealedCMOs()).isEqualTo(List.of(element(CMO_ID, cmo)));
        assertThat(caseData.getStandardDirectionOrder()).isEqualTo(sdo);
        assertThat(caseData.getUrgentHearingOrder()).isEqualTo(uho);
    }

    private CaseData buildCaseData(UUID selectedId, DocumentReference originalDocument) {
        return CaseData.builder()
            .id(1L)
            .state(SUBMITTED)
            .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersOperation(OrderOperation.AMEND)
                .manageOrdersAmendmentList(DynamicList.builder()
                    .value(DynamicListElement.builder().code(selectedId).build())
                    .build())
                .manageOrdersAmendedOrder(DOCUMENT_TO_STAMP)
                .manageOrdersOrderToAmend(originalDocument)
                .build())
            .orderCollection(List.of(element(ORDER_ID, ORDER)))
            .sealedCMOs(List.of(element(CMO_ID, CMO)))
            .urgentHearingOrder(UHO)
            .standardDirectionOrder(SDO)
            .build();
    }

    private CaseData extractCaseData(Map<String, Object> data) {
        return mapper.convertValue(data, CaseData.class);
    }
}
