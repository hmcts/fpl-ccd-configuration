package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.Map;

class EPOAddressPrePopulatorTest {

    private final EPOAddressPrePopulator underTest = new EPOAddressPrePopulator();

    @Test
    void testAcceptsEPORemovalAddressSection() {
        Assertions.assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.EPO_PREVENT_REMOVAL);
    }

    @Test
    void testAddressIsPrePopulatedWithExistingAddressInOrders() {
        final Address address = Address.builder().addressLine1("1 Street").postcode("SW1").build();
        CaseData caseData = CaseData.builder().orders(Orders.builder().address(address).build()).build();

        Assertions.assertThat(underTest.prePopulate(caseData))
            .containsOnly(Map.entry("manageOrdersEpoRemovalAddress", address));
    }

    @Test
    void testPrepopulateAddressWhenCaseDataDoesNotHaveOrders() {
        CaseData caseData = CaseData.builder().build();
        Assertions.assertThat(underTest.prePopulate(caseData)).isEmpty();
    }

    @Test
    void testPrepopulateAddressWhenOrdersDoesNotHaveAddress() {
        CaseData caseData = CaseData.builder().orders(Orders.builder().build()).build();
        Assertions.assertThat(underTest.prePopulate(caseData)).isEmpty();
    }
}
