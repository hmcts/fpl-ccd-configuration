package uk.gov.hmcts.reform.fpl.service.orders.amendment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.UploadDocumentService;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.action.AmendOrderAction;

import java.util.Collections;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.common.DocumentReference.buildFromDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

class AmendOrderServiceTest {

    private final CaseData caseData = mock(CaseData.class);
    private final ManageOrdersEventData eventData = mock(ManageOrdersEventData.class);

    private final AmendedOrderStamper stamper = mock(AmendedOrderStamper.class);
    private final AmendOrderAction action = mock(AmendOrderAction.class);
    private final UploadDocumentService uploadService = mock(UploadDocumentService.class);
    private final OthersService othersService = mock(OthersService.class);

    private final AmendOrderService underTest = new AmendOrderService(stamper, List.of(action), uploadService,
        othersService);

    @BeforeEach
    void setUp() {
        when(caseData.getManageOrdersEventData()).thenReturn(eventData);
    }

    @ParameterizedTest
    @ValueSource(strings = {"file.pdf", "amended_file.pdf"})
    void updateOrder(String filename) {
        DocumentReference originalOrder = mock(DocumentReference.class);
        DocumentReference uploadedOrder = mock(DocumentReference.class);
        byte[] stampedBinaries = new byte[]{1,2,3,4,5};
        Document stampedDocument = testDocument();
        DocumentReference amendedOrder = buildFromDocument(stampedDocument);
        Map<String, Object> amendedFields = Map.of("amendedCaseField", "some amended field");
        List<Element<Other>> selectedOthers = Collections.emptyList();

        when(action.accept(caseData)).thenReturn(true);

        when(eventData.getManageOrdersAmendedOrder()).thenReturn(uploadedOrder);
        when(stamper.amendDocument(uploadedOrder)).thenReturn(stampedBinaries);
        when(eventData.getManageOrdersOrderToAmend()).thenReturn(originalOrder);
        when(originalOrder.getFilename()).thenReturn(filename);
        when(uploadService.uploadDocument(stampedBinaries, "amended_file.pdf", "application/pdf"))
            .thenReturn(stampedDocument);

        when(action.applyAmendedOrder(caseData, amendedOrder, selectedOthers)).thenReturn(amendedFields);

        assertThat(underTest.updateOrder(caseData)).isEqualTo(amendedFields);
    }

    @Test
    void updateOrderNoActionFound() {
        DynamicList amendedOrderList = mock(DynamicList.class);
        String orderId = "some id";

        when(action.accept(caseData)).thenReturn(false);
        when(eventData.getManageOrdersAmendmentList()).thenReturn(amendedOrderList);
        when(amendedOrderList.getValueCode()).thenReturn(orderId);

        assertThatThrownBy(() -> underTest.updateOrder(caseData))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Could not find action to amend order for order with id \"some id\"");
    }
}
