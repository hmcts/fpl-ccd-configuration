package uk.gov.hmcts.reform.fpl.service.orders.history;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import static org.assertj.core.api.Assertions.assertThat;

class SealedOrderHistoryExtraTitleGeneratorTest {

    private static final String BLANK_ORDER_TITLE = "Blank order title";
    private static final String UPLOAD_OTHER_TITLE = "Upload other title";

    private final SealedOrderHistoryExtraTitleGenerator underTest = new SealedOrderHistoryExtraTitleGenerator();

    @Test
    void testIfBlankOrder() {
        String actual = underTest.generate(CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.C21_BLANK_ORDER)
                .manageOrdersTitle(BLANK_ORDER_TITLE)
                .build())
            .build());

        assertThat(actual).isEqualTo(BLANK_ORDER_TITLE);
    }

    @Test
    void testIfOtherUploadOrder() {
        String actual = underTest.generate(CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.OTHER_ORDER)
                .manageOrdersUploadTypeOtherTitle(UPLOAD_OTHER_TITLE)
                .build())
            .build());

        assertThat(actual).isEqualTo(UPLOAD_OTHER_TITLE);
    }

    @Test
    void testIfAnyOtherOrderType() {
        String actual = underTest.generate(CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(Order.C23_EMERGENCY_PROTECTION_ORDER)
                .manageOrdersUploadTypeOtherTitle(UPLOAD_OTHER_TITLE)
                .manageOrdersTitle(BLANK_ORDER_TITLE)
                .build())
            .build());

        assertThat(actual).isNull();
    }
}
