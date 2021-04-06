package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.section;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderSection;
import uk.gov.hmcts.reform.fpl.service.orders.generator.OrderDocumentGenerator;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DraftOrderPreviewSectionPrePopulatorTest {

    private static final Order ORDER = Order.C32_CARE_ORDER;
    private static final CaseDetails CASE_DETAILS = mock(CaseDetails.class);
    private final OrderDocumentGenerator orderDocumentGenerator = mock(OrderDocumentGenerator.class);

    private final DraftOrderPreviewSectionPrePopulator underTest = new DraftOrderPreviewSectionPrePopulator(
        orderDocumentGenerator);

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(OrderSection.REVIEW);
    }

    @Test
    void prePopulate() {
        Map<String, Object> actual = underTest.prePopulate(CaseData.builder()
            .manageOrdersType(ORDER)
            .build(), CASE_DETAILS);

        assertThat(actual).isEqualTo(Map.of());
        verify(orderDocumentGenerator).generate(ORDER, CASE_DETAILS);
    }
}
