package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Children;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.List;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
class CaseSubmissionValidatorServiceTest {

    private final CaseSubmissionValidatorService service = new CaseSubmissionValidatorService();

    @Test
    void shouldReturnErrorsIfMandatoryCaseSectionsHaveNotBeenCompleted() {
        CaseData caseData = CaseData.builder().build();
        List<String> errors = service.validateCaseDetails(caseData);

        assertThat(errors).contains("Enter a case name");
        assertThat(errors).contains("You need to add details to Orders and directions needed");
        assertThat(errors).contains("You need to add details to Children");
        assertThat(errors).contains("You need to add details to Applicant");
        assertThat(errors).contains("You need to add details to Hearing");
    }

    @Test
    void shouldReturnErrorsIfMandatoryFieldsHaveNotBeenCompleted() {

    }

    @Test
    void shouldReturnAnErrorWhenEPOHasBeenSelectedButNoReasonHasBeenGiven() {
        CaseData caseData = CaseData.builder()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EDUCATION_SUPERVISION_ORDER))
                .build())
            .build();

        List<String> errors = service.validateCaseDetails(caseData);

        assertThat(errors).contains("Select at least one option for how this case meets grounds for an emergency protection order");
    }

    @Test
    void shouldNotReturnErrorsIfMandatoryFieldsHaveBeenCompleted() {
        CaseData caseData = CaseData.builder()
            .caseName("Test case")
            .groundsForEPO(GroundsForEPO.builder()
                .reason(ImmutableList.of("reason"))
                .build())
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EDUCATION_SUPERVISION_ORDER))
                .build())
            .children(Children.builder()
                .firstChild(Child.builder()
                    .childName("Test")
                    .build())
                .additionalChildren(ImmutableList.of(
                    Element.<Child>builder()
                        .id(UUID.randomUUID())
                        .value(Child.builder()
                            .childName("Test")
                            .build())
                        .build()
                ))
                .build())
            .applicant(Applicant.builder()
                .name("Harry Kane")
                .personToContact("Harry Kane")
                .jobTitle("Judge")
                .address(Address.builder()
                    .addressLine1("1 Some street")
                    .addressLine2("Some road")
                    .postTown("some town")
                    .postcode("BT66 7RR")
                    .county("Some county")
                    .country("UK")
                    .build())
                .telephone("02838882404")
                .email("Harrykane@HMCTS.net")
                .build())
            .documents_checklist_document(Document.builder().build())
            .hearing(Hearing.builder()
                .type("Type")
                .build())
            .build();

        List<String> errors = service.validateCaseDetails(caseData);
        assertThat(errors).isEmpty();
    }
}
