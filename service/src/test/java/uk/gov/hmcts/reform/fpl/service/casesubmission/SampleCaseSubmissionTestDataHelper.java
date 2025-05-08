package uk.gov.hmcts.reform.fpl.service.casesubmission;

import uk.gov.hmcts.reform.fpl.enums.ChildRecoveryOrderGround;
import uk.gov.hmcts.reform.fpl.enums.ParticularsOfChildren;
import uk.gov.hmcts.reform.fpl.enums.PriorConsultationType;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationOrderGround;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationOrderSection;
import uk.gov.hmcts.reform.fpl.model.configuration.Language;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisAllocation;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApplicant;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC14Supplement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC15Supplement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC16Supplement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC17Supplement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC18Supplement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisC20Supplement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearing;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisHearingPreferences;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisInternationalElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisOtherParty;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisProceeding;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRisks;

import java.util.List;

import static java.time.LocalDate.now;
import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.List.of;
import static uk.gov.hmcts.reform.fpl.utils.AgeDisplayFormatHelper.formatAgeDisplay;

public class SampleCaseSubmissionTestDataHelper {

    private SampleCaseSubmissionTestDataHelper() {
    }

    private static final Language LANGUAGE = Language.ENGLISH;

    public static DocmosisCaseSubmission expectedDocmosisCaseSubmission() {
        return DocmosisCaseSubmission.builder()
            .respondents(expectedDocmosisRespondents())
            .applicants(expectedDocmosisApplicants())
            .children(expectedDocmosisChildren())
            .others(expectedDocmosisOtherParty())
            .courtName("Family Court")
            .applicantOrganisations("London Borough of Southwark")
            .respondentNames("Paul Smith\nJames Smith\nAn Other")
            .ordersNeeded("Emergency protection order")
            .directionsNeeded("Contact with any named person\nYes")
            .hearing(expectedDocmosisHearing())
            .allocation(expectedAllocation())
            .welshLanguageRequirement("No")
            .hearingPreferences(expectedDocmosisHearingPreferences())
            .internationalElement(expectedDocmosisInternationalElement())
            .courtSeal(null)
            .draftWaterMark(null)
            .userFullName("Professor")
            .submittedDate(now().format(ofPattern("d MMMM yyyy")))
            .groundsForEPOReason("There’s reasonable cause to believe the child is likely to suffer"
                + " significant harm if they’re not moved to accommodation provided by you, or on your behalf\n\n"
                + "There’s reasonable cause to believe the child is likely to suffer significant harm if they don’t "
                + "stay in their current accommodation")
            .groundsThresholdReason("The care given to the child not being what it would be "
                + "reasonable to expect a parent to give.\nChild is beyond parental control.")
            .thresholdDetails("Details")
            .risks(expectedDocmosisRisks())
            .proceeding(expectedDocmosisProceeding())
            .relevantProceedings("Yes")
            .build();
    }

    private static List<DocmosisRespondent> expectedDocmosisRespondents() {
        return of(DocmosisRespondent.builder()
                .name("Paul Smith")
                .age(formatAgeDisplay(parse("1944-05-02"), LANGUAGE))
                .gender("Male")
                .dateOfBirth("2 May 1944")
                .placeOfBirth("Crewe")
                .address("-")
                .contactDetailsHidden("No")
                .contactDetailsHiddenDetails("No")
                .telephoneNumber("07712345678")
                .relationshipToChild("Uncle")
                .litigationIssuesDetails("Yes\nrespondent one test litigation reason")
                .build(),
            DocmosisRespondent.builder()
                .name("James Smith")
                .age(formatAgeDisplay(parse("1933-04-02"), LANGUAGE))
                .gender("Unknown")
                .dateOfBirth("2 April 1933")
                .placeOfBirth("Newry")
                .address("Unit 12\nTransa Way\nHillsborough\nLurgan\nDown\nBT26 6RJ\nUnited Kingdom")
                .contactDetailsHidden("No")
                .contactDetailsHiddenDetails("No")
                .telephoneNumber("02892611016")
                .relationshipToChild("Brother")
                .litigationIssuesDetails("Yes\nrespondent two litigation capacity reason")
                .build(),
            DocmosisRespondent.builder()
                .name("An Other")
                .age(formatAgeDisplay(parse("1933-04-02"), LANGUAGE))
                .gender("Male")
                .dateOfBirth("2 April 1933")
                .placeOfBirth("Reading")
                .address("Flat 90\nSurrey street\nSurrey road\nSurrey\nCroydon\nBT22 2345\nUK")
                .contactDetailsHidden("-")
                .contactDetailsHiddenDetails("-")
                .telephoneNumber("0987654321")
                .relationshipToChild("Cousin")
                .litigationIssuesDetails("No")
                .build());
    }

    private static List<DocmosisApplicant> expectedDocmosisApplicants() {
        return of(DocmosisApplicant.builder()
            .organisationName("London Borough of Southwark")
            .contactName("Jane Smith")
            .address("160 Tooley St\nTooley road\nTooley\nLimerick\nGalway\nSE1 2QH\nIreland")
            .email("jane@smith.com")
            .mobileNumber("2020202020")
            .telephoneNumber("02120202020")
            .pbaNumber("PBA1234567")
            .solicitorName("Brian Banks")
            .solicitorMobile("7665545327")
            .solicitorTelephone("020 2772 5772")
            .solicitorEmail("brian@banks.com")
            .solicitorDx("106 Southwark &")
            .solicitorReference("12345")
            .build());
    }

    private static List<DocmosisChild> expectedDocmosisChildren() {
        return of(DocmosisChild.builder()
                .name("Tom Reeves")
                .age(formatAgeDisplay(parse("2018-06-15"), LANGUAGE))
                .gender("Male")
                .dateOfBirth("15 June 2018")
                .livingSituation("Living with respondents\nConfidential\nDate this began: 8 November 2018")
                .keyDates("child starting primary school or taking GCSEs")
                .careAndContactPlan("Place baby in local authority foster care")
                .adoption("Yes")
                .placementOrderApplication("Yes")
                .placementCourt("Central London County Court")
                .mothersName("Isbella Reeves")
                .fathersName("Rob Reeves")
                .fathersResponsibility("Yes")
                .socialWorkerName("Helen Green")
                .socialWorkerTelephoneNumber("0123456789")
                .additionalNeeds("Yes\nAutism")
                .litigationIssues("-")
                .detailsHiddenReason("Yes\nHistory of domestic violence with relatives")
                .build(),
            DocmosisChild.builder()
                .name("Sarah Reeves")
                .age(formatAgeDisplay(parse("2002-02-02"), LANGUAGE))
                .gender("Female")
                .dateOfBirth("2 February 2002")
                .livingSituation("Living with respondents\nCarnegie House\nCentral Milton Keynes\nMilton Keynes"
                    + "\nMK\nLondon\nMK10 1SA\nBT66 7RR\nDate this began: 2 February 2002")
                .keyDates("test child two key date")
                .careAndContactPlan("test child two care and contact plan")
                .adoption("Yes")
                .placementOrderApplication("Yes")
                .placementCourt("test child two court")
                .mothersName("Sarah Simpson")
                .fathersName("Scott Simpson")
                .fathersResponsibility("Yes")
                .socialWorkerName("Paul Wilsdon")
                .socialWorkerTelephoneNumber("07749972242")
                .additionalNeeds("Yes\nlearning disabilities")
                .litigationIssues("Yes\ntest child two ability to take part in proceedings")
                .detailsHiddenReason("No\ntest child two contact details hidden reason")
                .build());
    }

    private static List<DocmosisOtherParty> expectedDocmosisOtherParty() {
        return of(DocmosisOtherParty.builder()
                .name("Jason Lavery")
                .dateOfBirth("2 February 1999")
                .address("Flat 13\nPortadown street\nPortadown road\nPortadown\nArmagh\nBT43 343\nN.Ire")
                .telephoneNumber("07749972245")
                .relationshipToChild("Cousin")
                .litigationIssuesDetails("No")
                .build(),
            DocmosisOtherParty.builder()
                .name("Peter Lavery")
                .dateOfBirth("2 February 2000")
                .address("Confidential")
                .telephoneNumber("Confidential")
                .relationshipToChild("Friend of family")
                .litigationIssuesDetails("Yes\nOther two inability to take part in proceedings")
                .build());
    }

    private static DocmosisHearing expectedDocmosisHearing() {
        return DocmosisHearing.builder()
            .timeFrame("Same day\nReason: Baby will be discharged from hospital on 21 June 2018")
            .withoutNoticeDetails("Yes\nReason: Notice without hearing needed")
            .respondentsAware("Yes")
            .respondentsAwareReason("They seek to care for baby in mother and baby unit")
            .build();
    }

    private static DocmosisAllocation expectedAllocation() {
        return DocmosisAllocation.builder()
            .proposal("Circuit judge")
            .proposalReason("allocation proposal reason")
            .build();
    }

    private static DocmosisHearingPreferences expectedDocmosisHearingPreferences() {
        return DocmosisHearingPreferences.builder()
            .interpreter("Interpreter required")
            .intermediary("Intermediary hearing required")
            .disabilityAssistance("Learning disability")
            .extraSecurityMeasures("Separate waiting room required")
            .somethingElse("I need this from someone")
            .build();
    }

    private static DocmosisInternationalElement expectedDocmosisInternationalElement() {
        return DocmosisInternationalElement.builder()
            .whichCountriesInvolved("Italy, Spain, France")
            .outsideHagueConvention("Yes")
            .importantDetails("Something happened in France")
            .build();
    }

    private static DocmosisRisks expectedDocmosisRisks() {
        return DocmosisRisks.builder()
            .physicalHarm("Yes")
            .emotionalHarm("Yes")
            .sexualAbuse("Yes")
            .neglect("Yes")
            .alcoholDrugAbuse("Yes")
            .domesticAbuse("Yes")
            .anythingElse("Something else")
            .build();
    }

    private static List<DocmosisProceeding> expectedDocmosisProceeding() {
        return List.of(DocmosisProceeding.builder()
                .proceedingStatus("Ongoing")
                .caseNumber("12345")
                .started("02-02-2002")
                .ended("02-02-2006")
                .ordersMade("Supervision order")
                .judge("William Peters")
                .children("children subject to proceedings")
                .guardian("Mark Watson")
                .sameGuardianDetails("No\nSome guardian not needed")
                .build(),
            DocmosisProceeding.builder()
                .proceedingStatus("Ongoing")
                .caseNumber("12345")
                .started("02-02-2008")
                .ended("02-02-2009")
                .ordersMade("Supervision order")
                .judge("Peters Williams")
                .children("children subject to proceedings")
                .guardian("John Watson")
                .sameGuardianDetails("No\nSome guardian not needed")
                .build());
    }

    public static DocmosisC14Supplement expectedDocmosisC14Supplement() {
        return DocmosisC14Supplement.builder()
            .childrensNames("Bobby Smith")
            .caseNumber("01234567890")
            .personHasContactAndCurrentArrangement("Someone was allowed to contact the child twice per month.")
            .laHasRefusedContact("LA has refused contact for 7 days")
            .personsBeingRefusedContactWithChild("Someone is not allowed to contact the child anymore.")
            .reasonsOfApplication("Reason of the application")
            .build();
    }

    public static DocmosisC15Supplement expectedDocmosisC15Supplement() {
        return DocmosisC15Supplement.builder()
            .childrensNames("Bobby Smith")
            .caseNumber("01234567890")
            .parentOrGuardian("Parent")
            .residenceOrder("Yes")
            .hadCareOfChildrenBeforeCareOrder("Yes")
            .reasonsForApplication("Reason for application")
            .build();
    }

    public static DocmosisC16Supplement expectedDocmosisC16Supplement() {
        return DocmosisC16Supplement.builder()
            .childrensNames("Bobby Smith")
            .caseNumber("01234567890")
            .directionsSoughtAssessment("Directions sought in respect of assessment")
            .directionsSoughtContact("Directions sought in respect of contact")
            .groundsForChildAssessmentOrderReason("Reason for the grounds being met")
            .build();
    }

    public static DocmosisC17Supplement expectedDocmosisC17Supplement() {
        return DocmosisC17Supplement.builder()
            .childrensNames("Bobby Smith")
            .caseNumber("01234567890")
            .isOrAre("is")
            .childOrChildren("child")
            .priorConsultationOtherLA("Other LA")
            .priorConsultationType(List.of(PriorConsultationType.PROVIDE_ACCOMMODATION.getLabel()))
            .groundReason("Ground detail")
            .directionsAppliedFor("directionApplied")
            .build();
    }

    public static DocmosisC18Supplement expectedDocmosisC18Supplement() {
        return DocmosisC18Supplement.builder()
            .childrenNames("Bobby Smith")
            .caseNumber("01234567890")
            .isOrAre("is")
            .childOrChildren("child")
            .particularsOfChildren(List.of(ParticularsOfChildren.IN_CARE.getLabel()))
            .particularsOfChildrenDetails("particularsOfChildrenDetails")
            .grounds(List.of(ChildRecoveryOrderGround.RUN_AWAY_FROM_RESPONSIBLE_PERSON.getLabel()))
            .reason("Reason for the grounds being met")
            .build();
    }

    public static DocmosisC20Supplement expectedDocmosisC20Supplement() {
        return DocmosisC20Supplement.builder()
            .childrensNames("Bobby Smith")
            .caseNumber("01234567890")
            .section(SecureAccommodationOrderSection.ENGLAND.getLabel())
            .grounds(List.of(SecureAccommodationOrderGround.APPROVAL_OF_SECRETARY_OF_STATE.getLabel()))
            .reasonAndLength("Reason for the grounds being met")
            .build();
    }
}
