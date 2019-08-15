package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Children;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class CaseSubmissionValidatorServiceTest {

    private final CaseSubmissionValidatorService service = new CaseSubmissionValidatorService();

    @Test
    void shouldReturnErrorsIfMandatoryCaseSectionsHaveNotBeenCompleted() {
        CaseData caseData = CaseData.builder().build();
        List<String> errors = service.validateCaseDetails(caseData);

        assertThat(errors).containsOnly("In the case name section:",
            "- Enter a case name",
            "In the orders section:",
            "- Select at least one type of order",
            "In the children section:",
            "- You need to add details to children",
            "In the applicant section:",
            "- You need to add details to applicant",
            "In the hearing section:",
            "- You need to add details to hearing",
            "In the documents section:",
            "- Tell us the status of all documents including those that you haven't uploaded"
        );
    }

    @Test
    void shouldReturnErrorsIfMandatoryFieldsHaveNotBeenCompleted() {
        CaseData caseData = CaseData.builder()
            .caseName("Test case")
            .children(Children.builder().build())
            .hearing(Hearing.builder().build())
            .applicant(Applicant.builder().build())
            .build();

        List<String> errors = service.validateCaseDetails(caseData);

        assertThat(errors).containsOnly("In the orders section:",
            "- Select at least one type of order",
            "- Select an option for when you need a hearing",
            "In the applicant section:",
            "- Enter the applicant's full name",
            "- Enter the contact's full name",
            "- Enter a job title for the contact",
            "- Enter a valid address for the contact",
            "- Enter at least one telephone number for the contact",
            "- Enter an email address for the contact",
            "In the children section:",
            "- Tell us the names of all children in the case",
            "In the documents section:",
            "- Tell us the status of all documents including those that you haven't uploaded",
            "In the hearing section:",
            "- Select an option for when you need a hearing"
        );
    }

    @Test
    void shouldReturnAnErrorWhenEPOHasBeenSelectedButNoGroundsForTheApplicationProvided() {
        CaseData caseData = CaseData.builder()
            .caseName("Test case")
            .hearing(Hearing.builder()
                .timeFrame("Within 18 days")
                .build())
            .children(Children.builder()
                .firstChild(Child.builder()
                    .childName("Child name")
                    .build())
                .build())
            .applicant(Applicant.builder()
                .telephone("12345")
                .email("bran@winterfell.com")
                .name("Bran Stark")
                .personToContact("Sansa Stark")
                .jobTitle("Warden of the north")
                .address(Address.builder()
                    .addressLine1("Winterfell castle")
                    .postcode("BT66 7RR")
                    .build())
                .build())
            .documents_socialWorkStatement_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_checklist_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkAssessment_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkCarePlan_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkChronology_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkEvidenceTemplate_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_threshold_document(Document.builder()
                .documentStatus("reason")
                .build())
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .build();

        List<String> errors = service.validateCaseDetails(caseData);

        assertThat(errors).containsOnly("In the grounds for the application section:",
            "- Select at least one option for how this case meets grounds for an emergency protection order",
            "- Select at least one option for how this case meets the threshold criteria",
            "- Enter details of how the case meets the threshold criteria"
        );
    }

    @Test
    void shouldReturnAnErrorIfApplicantIsPopulatedButAddressIsPartiallyCompleted() {
        CaseData caseData = CaseData.builder()
            .caseName("Test case")
            .hearing(Hearing.builder()
                .timeFrame("Within 18 days")
                .build())
            .children(Children.builder()
                .firstChild(Child.builder()
                    .childName("Child name")
                    .build())
                .build())
            .applicant(Applicant.builder()
                .telephone("12345")
                .email("bran@winterfell.com")
                .name("Bran Stark")
                .personToContact("Sansa Stark")
                .jobTitle("Warden of the north")
                .address(Address.builder()
                    .addressLine2("Winterfell castle")
                    .build())
                .build())
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .documents_socialWorkStatement_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_checklist_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkAssessment_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkCarePlan_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkChronology_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkEvidenceTemplate_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_threshold_document(Document.builder()
                .documentStatus("reason")
                .build())
            .build();

        List<String> errors = service.validateCaseDetails(caseData);
        assertThat(errors).contains("- Enter a valid address for the contact");
        assertThat(errors).contains("- Enter a postcode for the contact");
    }

    @Test
    void shouldNotReturnErrorsIfMandatoryFieldsHaveBeenCompletedNotIncludingEPO() {
        CaseData caseData = CaseData.builder()
            .caseName("Test case")
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
            .documents_socialWorkStatement_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_checklist_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkAssessment_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkCarePlan_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkChronology_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkEvidenceTemplate_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_threshold_document(Document.builder()
                .documentStatus("reason")
                .build())
            .hearing(Hearing.builder()
                .timeFrame("Within 18 days")
                .build())
            .build();

        List<String> errors = service.validateCaseDetails(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsIfMandatoryFieldsHaveBeenCompleted() {
        CaseData caseData = CaseData.builder()
            .caseName("Test case")
            .groundsForEPO(GroundsForEPO.builder()
                .reason(ImmutableList.of("reason"))
                .thresholdReason(ImmutableList.of("reason"))
                .thresholdDetails("Details")
                .build())
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
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
            .documents_socialWorkStatement_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_checklist_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkAssessment_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkCarePlan_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkChronology_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_socialWorkEvidenceTemplate_document(Document.builder()
                .documentStatus("reason")
                .build())
            .documents_threshold_document(Document.builder()
                .documentStatus("reason")
                .build())
            .hearing(Hearing.builder()
                .timeFrame("Within 18 days")
                .build())
            .build();

        List<String> errors = service.validateCaseDetails(caseData);
        assertThat(errors).isEmpty();
    }
}
