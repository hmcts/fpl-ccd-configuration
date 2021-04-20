package uk.gov.hmcts.reform.fpl.service.orders.validator;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock.EPO_REMOVAL_ADDRESS;

class EPORemovalAddressValidatorTest {

    private static final String INVALID_ADDRESS_LINE = "Enter a valid address for the contact";
    private static final String INVALID_POST_CODE = "Enter a postcode for the contact";

    private final EPORemovalAddressValidator underTest = new EPORemovalAddressValidator();

    @Test
    void accept() {
        AssertionsForClassTypes.assertThat(underTest.accept()).isEqualTo(EPO_REMOVAL_ADDRESS);
    }

    @Test
    void validateRemovalAddress() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEpoRemovalAddress(Address.builder().addressLine1("address 1").postcode("SW1").build())
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEmpty();
    }

    @Test
    void validateInvalidAddress() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEpoRemovalAddress(Address.builder().build())
                .build())
            .build();

        assertThat(underTest.validate(caseData))
            .isEqualTo(List.of(INVALID_ADDRESS_LINE, INVALID_POST_CODE));
    }

    @Test
    void validateInvalidPostCode() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEpoRemovalAddress(Address.builder().addressLine1("123").build())
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(INVALID_POST_CODE));
    }

    @Test
    void validateInvalidAddressLine1() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEpoRemovalAddress(Address.builder().postcode("SW1").build())
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(INVALID_ADDRESS_LINE));
    }

    @Test
    void shouldNotValidateWhenEPOTypeIsRemoveToAccommodation() {
        CaseData caseData = CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersEpoType(EPOType.REMOVE_TO_ACCOMMODATION)
                .manageOrdersEpoRemovalAddress(Address.builder().postcode("").build())
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEmpty();
    }
}
