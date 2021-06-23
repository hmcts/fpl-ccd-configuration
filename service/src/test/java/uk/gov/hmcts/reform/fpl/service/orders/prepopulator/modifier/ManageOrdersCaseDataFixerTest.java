package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.modifier;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;

import static org.assertj.core.api.Assertions.assertThat;

class ManageOrdersCaseDataFixerTest {

    private final ManageOrdersCaseDataFixer underTest = new ManageOrdersCaseDataFixer();

    @Test
    void shouldNotModifyIfNotClosedState() {
        CaseData actual = underTest.fix(CaseData.builder().build());

        assertThat(actual).isEqualTo(CaseData.builder().build());
    }

    @Test
    void defaultBlankOrderIfInClosedState() {
        CaseData actual = underTest.fix(CaseData.builder().state(State.CLOSED).build());

        assertThat(actual).isEqualTo(CaseData.builder()
            .state(State.CLOSED)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(
                    Order.C21_BLANK_ORDER).build())
            .build());
    }
}
