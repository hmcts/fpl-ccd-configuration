package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeAll;
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
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.validation.groups.EPOGroup;

import java.util.List;
import java.util.UUID;
import javax.validation.Validation;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createPopulatedChildren;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;

@ExtendWith(SpringExtension.class)
class CaseValidatorServiceTest {
    private static CaseValidatorService caseValidatorService;

    @BeforeAll()
    static void setup() {
        caseValidatorService = new CaseValidatorService(Validation
            .buildDefaultValidatorFactory()
            .getValidator());
    }

    @Test
    void shouldReturnErrorsWhenMandatoryCaseSectionsHaveNotBeenCompleted() {
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
            "• You need to add details to solicitor",
            "In the hearing needed section:",
            "• You need to add details to hearing needed",
            "In the documents section:",
            "• Tell us the status of all documents including those that you haven't uploaded",
            "In the grounds for the application section:",
            "• You need to add details to grounds for the application"
        );
    }

    @Test
    void shouldReturnErrorsWhenMandatoryFieldsHaveNotBeenCompleted() {
        CaseData caseData = emptyMandatoryCaseData();
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
            "• Enter the solicitor's full name",
            "• Enter the solicitor's email",
            "In the children section:",
            "• Tell us the names of all children in the case",
            "In the documents section:",
            "• Tell us the status of all documents including those that you haven't uploaded",
            "In the hearing needed section:",
            "• Select an option for when you need a hearing"
        );
    }

    @Test
    void shouldReturnAnErrorWhenApplicantIsPopulatedButAddressIsPartiallyCompleted() {
        CaseData caseData = partiallyCompleteCaseData()
            .applicants(applicants(false))
            .build();

        List<String> errors = caseValidatorService.validateCaseDetails(caseData);
        assertThat(errors).containsOnlyOnce(
            "• Enter a valid address for the contact",
            "• Enter a postcode for the contact"
        );
    }

    @Test
    void shouldNotReturnErrorsWhenMandatoryFieldsHaveBeenCompletedNotIncludingEPO() {
        CaseData caseData = partiallyCompleteCaseData()
            .applicants(applicants(true))
            .solicitor(solicitor())
            .grounds(grounds())
            .build();

        List<String> errors = caseValidatorService.validateCaseDetails(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenMandatoryFieldsHaveBeenCompleted() {
        CaseData caseData = partiallyCompleteCaseData()
            .applicants(applicants(true))
            .grounds(grounds())
            .solicitor(solicitor())
            .groundsForEPO(GroundsForEPO.builder()
                .reason(ImmutableList.of("reason"))
                .build())
            .build();

        List<String> errors = caseValidatorService.validateCaseDetails(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnAnErrorWhenEPOHasBeenSelectedButNoGroundsForTheApplicationProvided() {
        CaseData caseData = caseDocuments().orders(orders()).build();
        List<String> errors = caseValidatorService.validateCaseDetails(caseData, EPOGroup.class);

        assertThat(errors).containsOnlyOnce(
            "In the grounds for the application section:",
            "• You need to add details to grounds for the application"
        );
    }

    @Test
    void shouldReturnAnErrorWhenEPOHasBeenSelectedButGroundsIsEmpty() {
        CaseData caseData = caseDocuments()
            .orders(orders())
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
        CaseData caseData = caseDocuments()
            .orders(orders())
            .grounds(grounds())
            .groundsForEPO(GroundsForEPO.builder()
                .reason(ImmutableList.of("reason"))
                .build())
            .build();

        List<String> errors = caseValidatorService.validateCaseDetails(caseData, EPOGroup.class);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnAnErrorWhenFirstRespondentHasFullNameButNotSecondRespondent() {
        CaseData caseData = partiallyCompleteCaseData()
            .grounds(grounds())
            .applicants(applicants(true))
            .solicitor(solicitor())
            .respondents1(respondents())
            .build();

        List<String> errors = caseValidatorService.validateCaseDetails(caseData);
        assertThat(errors).isEmpty();
    }

    private CaseData emptyMandatoryCaseData() {
        return CaseData.builder()
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
            .solicitor(Solicitor.builder().build())
            .build();
    }

    private CaseData.CaseDataBuilder partiallyCompleteCaseData() {
        return caseDocuments()
            .caseName("Test case")
            .hearing(hearing())
            .respondents1(createRespondents())
            .children1(createPopulatedChildren())
            .orders(orders());
    }

    private CaseData.CaseDataBuilder caseDocuments() {
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

    private List<Element<Respondent>> respondents() {
        return ImmutableList.of(
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder().party(
                    RespondentParty.builder()
                        .firstName("Timothy")
                        .lastName("Jones")
                        .build())
                    .build())
                .build(),
            Element.<Respondent>builder()
                .id(UUID.randomUUID())
                .value(Respondent.builder().party(
                    RespondentParty.builder()
                        .firstName("Sarah")
                        .build())
                    .build())
                .build());
    }

    private List<Element<Applicant>> applicants(boolean hasCompletedAddress) {
        Address.AddressBuilder addressBuilder = Address.builder();

        addressBuilder.addressLine2("Some road");

        if (hasCompletedAddress) {
            addressBuilder.addressLine1("1 Some street")
                .postTown("some town")
                .postcode("BT66 7RR")
                .county("Some county")
                .country("UK");
        }

        return List.of(Element.<Applicant>builder()
            .id(UUID.randomUUID())
            .value(Applicant.builder()
                .leadApplicantIndicator("Yes")
                .party(ApplicantParty.builder()
                    .organisationName("Harry Kane")
                    .jobTitle("Judge")
                    .address(addressBuilder.build())
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

    private Solicitor solicitor() {
        return Solicitor.builder().name("fred").email("fred@fred.me").build();
    }

    private Orders orders() {
        return Orders.builder()
            .orderType(ImmutableList.of(OrderType.EMERGENCY_PROTECTION_ORDER))
            .build();
    }

    private Hearing hearing() {
        return Hearing.builder()
            .timeFrame("Within 18 days")
            .build();
    }

    private Grounds grounds() {
        return Grounds.builder()
            .thresholdDetails("details")
            .thresholdReason(ImmutableList.of("reason"))
            .build();
    }
}
