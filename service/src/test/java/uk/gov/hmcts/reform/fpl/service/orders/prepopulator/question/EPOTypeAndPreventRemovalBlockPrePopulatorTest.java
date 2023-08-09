package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;

class EPOTypeAndPreventRemovalBlockPrePopulatorTest {

    private final EPOTypeAndPreventRemovalBlockPrePopulator underTest = new EPOTypeAndPreventRemovalBlockPrePopulator();

    @Test
    void testAcceptsEPORemovalAddressSection() {
        assertThat(underTest.accept()).isEqualTo(OrderQuestionBlock.EPO_TYPE_AND_PREVENT_REMOVAL);
    }

    @Test
    void testEPOTypeAndPreventRemovalFieldsArePrePopulatedFromOrdersData() {
        final Address address = Address.builder().addressLine1("1 Street").postcode("SW1").build();
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder().address(address)
                .orderType(List.of(EMERGENCY_PROTECTION_ORDER))
                .epoType(EPOType.PREVENT_REMOVAL)
                .excluded("John").build())
            .build();

        assertThat(underTest.prePopulate(caseData)).isEqualTo(
            Map.of("manageOrdersEpoRemovalAddress", address,
                "manageOrdersEpoType", EPOType.PREVENT_REMOVAL,
                "manageOrdersWhoIsExcluded", "John",
                "manageOrdersExclusionRequirement", "Yes"));
    }

    @Test
    void testOnlyEPOAddressAndEPOTypeFieldsArePrePopulatedWhenEPOTypeInOrdersIsRemoveToAccommodation() {
        final Address address = Address.builder().addressLine1("1 Street").postcode("SW1").build();
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder().address(address)
                .orderType(List.of(EMERGENCY_PROTECTION_ORDER))
                .epoType(EPOType.REMOVE_TO_ACCOMMODATION).build())
            .build();

        assertThat(underTest.prePopulate(caseData)).isEqualTo(
            Map.of("manageOrdersEpoRemovalAddress", address,
                "manageOrdersEpoType", EPOType.REMOVE_TO_ACCOMMODATION));
    }

    @Test
    void testPrepopulateAddressWhenCaseDataDoesNotHaveOrders() {
        CaseData caseData = CaseData.builder().build();
        assertThat(underTest.prePopulate(caseData)).isEmpty();
    }

}
