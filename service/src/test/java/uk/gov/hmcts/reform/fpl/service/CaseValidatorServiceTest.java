package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.validators.interfaces.EPOGroup;

import java.util.List;

import java.util.UUID;

import javax.validation.Validation;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class CaseValidatorServiceTest {
    private CaseValidatorService caseValidatorService;

    @BeforeEach()
    private void setup() {
        caseValidatorService = new CaseValidatorService(Validation
            .buildDefaultValidatorFactory()
            .getValidator());
    }

    @Test
    void shouldReturnErrorsIfMandatoryCaseSectionsHaveNotBeenCompleted() {
        CaseData caseData = CaseData.builder().build();
        List<String> errors = caseValidatorService.validateCaseDetails(caseData);

        assertThat(errors).containsOnlyOnce(
            "In the case name section:",
            "• Enter a case name",
            "In the orders and directions needed section:",
            "• You need to add details to orders and directions needed",
            "In the children section:",
            "• You need to add details to children",
            "In the applicant section:",
            "• You need to add details to applicant",
            "In the hearing needed section:",
            "• You need to add details to hearing needed",
            "In the documents section:",
            "• Tell us the status of all documents including those that you haven't uploaded",
            "In the grounds for the application section:",
            "• You need to add details to grounds for the application"
        );
    }

    @Test
    void shouldReturnErrorsIfMandatoryFieldsHaveNotBeenCompleted() {
        CaseData caseData = CaseData.builder()
            .caseName("Test case")
            .children1(List.of(Element.<Child>builder()
                .id(UUID.randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder().build())
                    .build())
                .build()))
            .hearing(Hearing.builder().build())
            .applicants(List.of(Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .party(ApplicantParty.builder().build())
                    .build())
                .build()))
            .build();

        List<String> errors = caseValidatorService.validateCaseDetails(caseData);

        assertThat(errors).containsOnlyOnce(
            "In the orders and directions needed section:",
            "• You need to add details to orders and directions needed",
            "• Select an option for when you need a hearing",
            "In the applicant section:",
            "• Enter the applicant's full name",
            "• Enter the contact's full name",
            "• Enter a job title for the contact",
            "• Enter a valid address for the contact",
            "• Enter at least one telephone number for the contact",
            "• Enter an email address for the contact",
            "In the children section:",
            "• Tell us the names of all children in the case",
            "In the documents section:",
            "• Tell us the status of all documents including those that you haven't uploaded",
            "In the hearing needed section:",
            "• Select an option for when you need a hearing"
        );
    }

    @Test
    void shouldReturnAnErrorIfApplicantIsPopulatedButAddressIsPartiallyCompleted() {
        CaseData caseData = initCaseDocuments()
            .caseName("Test case")
            .hearing(Hearing.builder()
                .timeFrame("Within 18 days")
                .build())
            .children1(initChildren())
            .applicants(List.of(Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .party(ApplicantParty.builder()
                        .email(EmailAddress.builder()
                            .email("bran@winterfell.com")
                            .build())
                        .address(Address.builder()
                            .addressLine2("Winterfell castle")
                            .build())
                        .telephoneNumber(Telephone.builder()
                            .telephoneNumber("12345")
                            .contactDirection("Sansa Stark")
                            .build())
                        .jobTitle("Warden of the north")
                        .firstName("Bran")
                        .lastName("Stark")
                        .build())
                    .leadApplicantIndicator("Yes")
                    .build())
                .build()))
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .build();

        List<String> errors = caseValidatorService.validateCaseDetails(caseData);
        assertThat(errors).containsOnlyOnce(
            "• Enter a valid address for the contact",
            "• Enter a postcode for the contact"
        );
    }

    @Test
    void shouldNotReturnErrorsIfMandatoryFieldsHaveBeenCompletedNotIncludingEPO() {
        CaseData caseData = initCaseDocuments()
            .caseName("Test case")
            .grounds(Grounds.builder()
                .thresholdReason(ImmutableList.of("reason"))
                .thresholdDetails("details")
                .build())
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EDUCATION_SUPERVISION_ORDER))
                .build())
            .children1(initChildren())
            .applicants(initApplicants())
            .hearing(Hearing.builder()
                .timeFrame("Within 18 days")
                .build())
            .build();

        List<String> errors = caseValidatorService.validateCaseDetails(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsIfMandatoryFieldsHaveBeenCompleted() {
        CaseData caseData = initCaseDocuments()
            .caseName("Test case")
            .groundsForEPO(GroundsForEPO.builder()
                .reason(ImmutableList.of("reason"))
                .build())
            .grounds(Grounds.builder()
                .thresholdReason(ImmutableList.of("reason"))
                .thresholdDetails("Details")
                .build())
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .children1(initChildren())
            .applicants(initApplicants())
            .hearing(Hearing.builder()
                .timeFrame("Within 18 days")
                .build())
            .build();

        List<String> errors = caseValidatorService.validateCaseDetails(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnAnErrorWhenEPOHasBeenSelectedButNoGroundsForTheApplicationProvided() {
        CaseData caseData = initCaseDocuments()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .build();

        List<String> errors = caseValidatorService.validateCaseDetails(caseData, EPOGroup.class);

        assertThat(errors).containsOnlyOnce(
            "In the grounds for the application section:",
            "• You need to add details to grounds for the application"
        );
    }

    @Test
    void shouldReturnAnErrorWhenEPOHasBeenSelectedButGroundsIsEmpty() {
        CaseData caseData = initCaseDocuments()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .groundsForEPO(GroundsForEPO.builder().build())
            .build();

        List<String> errors = caseValidatorService.validateCaseDetails(caseData, EPOGroup.class);

        assertThat(errors).containsOnlyOnce(
            "In the grounds for the application section:",
            "• Select at least one option for how this case meets grounds for an emergency protection order"
        );
    }

    @Test
    void shouldNotReturnAnErrorWhenGroundsAndGroundsForEPOAreCompleted() {
        CaseData caseData = initCaseDocuments()
            .orders(Orders.builder()
                .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
                .build())
            .grounds(Grounds.builder()
                .thresholdDetails("details")
                .thresholdReason(ImmutableList.of("reason"))
                .build())
            .groundsForEPO(GroundsForEPO.builder()
                .reason(ImmutableList.of("reason"))
                .build())
            .build();

        List<String> errors = caseValidatorService.validateCaseDetails(caseData, EPOGroup.class);
        assertThat(errors).isEmpty();
    }

    private CaseData.CaseDataBuilder initCaseDocuments() {
        return CaseData.builder()
            .socialWorkStatementDocument(Document.builder()
                .documentStatus("reason")
                .build())
            .checklistDocument(Document.builder()
                .documentStatus("reason")
                .build())
            .socialWorkAssessmentDocument(Document.builder()
                .documentStatus("reason")
                .build())
            .socialWorkCarePlanDocument(Document.builder()
                .documentStatus("reason")
                .build())
            .socialWorkChronologyDocument(Document.builder()
                .documentStatus("reason")
                .build())
            .socialWorkEvidenceTemplateDocument(Document.builder()
                .documentStatus("reason")
                .build())
            .thresholdDocument(Document.builder()
                .documentStatus("reason")
                .build());
    }

    private List<Element<Applicant>> initApplicants() {
        return List.of(Element.<Applicant>builder()
            .id(UUID.randomUUID())
            .value(Applicant.builder()
                .leadApplicantIndicator("Yes")
                .party(ApplicantParty.builder()
                    .organisationName("Harry Kane")
                    .jobTitle("Judge")
                    .address(Address.builder()
                        .addressLine1("1 Some street")
                        .addressLine2("Some road")
                        .postTown("some town")
                        .postcode("BT66 7RR")
                        .county("Some county")
                        .country("UK")
                        .build())
                    .email(EmailAddress.builder()
                        .email("Harrykane@hMCTS.net")
                        .build())
                    .telephoneNumber(Telephone.builder()
                        .telephoneNumber("02838882404")
                        .contactDirection("Harry Kane")
                        .build())
                    .build())
                .build())
            .build()
        );
    }

    private List<Element<Child>> initChildren() {
        return ImmutableList.of(
            Element.<Child>builder()
                .id(UUID.randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("James")
                        .lastName("Nelson")
                        .build())
                    .build())
                .build()
        );
    }
}
