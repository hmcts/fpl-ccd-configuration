package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C23_EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class C23EPOAdditionalDocumentsCollectorTest {

    private static final Order ORDER_TYPE = C23_EMERGENCY_PROTECTION_ORDER;

    private C23EPOAdditionalDocumentsCollector underTest = new C23EPOAdditionalDocumentsCollector();

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(ORDER_TYPE);
    }

    @Test
    void testAdditionalDocuments() {
        final DocumentReference documentReference = testDocumentReference();
        CaseData caseData = CaseData.builder().manageOrdersEventData(
            ManageOrdersEventData.builder()
                .manageOrdersPowerOfArrest(documentReference).build()).build();
        assertThat(underTest.additionalDocuments(caseData)).isEqualTo(List.of(documentReference));
    }

    @Test
    void testAdditionalDocumentsReturnsEmptyList() {
        CaseData caseData = CaseData.builder().manageOrdersEventData(
            ManageOrdersEventData.builder().build()).build();
        assertThat(underTest.additionalDocuments(caseData)).isEqualTo(List.of());
    }

}
